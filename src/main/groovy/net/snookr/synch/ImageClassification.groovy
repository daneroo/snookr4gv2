/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snookr.synch
import net.snookr.db.Database;
import net.snookr.db.FSImageDAO;
import net.snookr.db.FlickrImageDAO;
import net.snookr.flickr.Photos;
import net.snookr.synch.Filesystem2Database;
import net.snookr.synch.Flickr2Database;
import net.snookr.util.DateFormat;
import net.snookr.util.Exif;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.exif.ExifDirectory;
import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 *
 * @author daniel
 */
class ImageClassification {
    String cameraS30 = "Canon|Canon PowerShot S30";
    String cameraSD300 = "Canon|Canon PowerShot SD300";
    String cameraREBELXT = "Canon|Canon EOS DIGITAL REBEL XT";
    String cameraOfInterest = "Canon|Canon PowerShot S30";
    def verbose=false;
    public void run() {
        println "Hello Image Classification";
        List fsimaList = getList();
        // show hosto by camera
        showHisto(fsimaList,{ fsima -> //
                fsima.camera;
            });
        // show hosto by ~owner
        Closure ownerMapper = { fsima -> //
            File f = new File(fsima.fileName);

            String[] dirs = fsima.fileName.split("/");
            String rootDir = dirs[4];
            String month = DateFormat.format(fsima.taken).substring(0,4);

            String owner = rootDir+":"+month+":"+fsima.camera+":"+Exif.getCameraOwner(f);
            owner;
        };
        showHisto(filterCamera(fsimaList,cameraS30),ownerMapper);
        showHisto(filterPathS30(fsimaList,"dad"),ownerMapper);

        showChains(filterPathS30(fsimaList,"dad"));
        showChains(filterPathS30(fsimaList,"cat"));
        showChains(filterCamera(fsimaList,cameraS30));
    }

    String rootDir(String fileName){
        String[] dirs = fileName.split("/");
        return dirs[4];
    }
    List filterPathS30(List list,pathRootPrefix){
        return list.findAll(){ fsima -> //
            fsima.camera == cameraS30 && rootDir(fsima.fileName).startsWith(pathRootPrefix)
        }
    }
    List filterCamera(List list,String camera){
        return list.findAll(){ fsima -> //
            fsima.camera == camera
        }
    }
    private int parseNumFromFileName(String fileName){
        File f = new File(fileName);
        String baseName = f.getName();
        def patterns = [ // list of Pattern's'
            ~/\d+-(\d+)_IMG.JPG/,
            ~/IMG_(\d+).JPG/,
            ~/IMG_(\d+).orig.JPG/,
            ~/ST[ABCD]_(\d)+.JPG/,
        ];
        Pattern matchedPattern = null;
        int imageNumber = -1;
        patterns.each(){ pattern -> //
            Matcher matcher = pattern.matcher(baseName);
            if (matcher.matches()){
                matchedPattern = pattern;
                imageNumber = Integer.parseInt(matcher[0][1]);
            }
        }
        return imageNumber;
    }
    private void showChains(List list){
        List chains = [];
        List lastImageNumberForChain = [];
        int maxDelta=100;
        int maxDeltaDays=45;
        list.each(){ fsima -> //
            File f = new File(fsima.fileName);
            String baseName = f.getName();
            int imageNumber = parseNumFromFileName(fsima.fileName);
            String owner = Exif.getCameraOwner(f);

            //println("Classify : ${fsima}");
            //which chain ?
            int whichChain=-1;
            int minDelta=9999999;
            chains.eachWithIndex(){ chain,c -> //
                //println("Consider chain:${c} [${chain.size()}]");
                int prevImageNumber=lastImageNumberForChain[c];
                int delta = imageNumber-prevImageNumber;
                def timeDeltaDays = (fsima.taken.getTime()-chain[-1].taken.getTime())/1000/60/60/24;
                def prevOwner = Exif.getCameraOwner(new File(chain[-1].fileName));
                // prefer later chains: = in delta<=minDelta
                // same imagenumber is posibble
                if (delta>=0 && delta<=maxDelta && delta<=minDelta && timeDeltaDays<maxDeltaDays && owner==prevOwner ){
                    minDelta=delta;
                    whichChain=c;
                }
            }
            if (whichChain==-1) {
                chains << []; // add a Chain
                whichChain=chains.size()-1;
                println("  HEAD : ${baseName} : ${DateFormat.format(fsima.taken)} : ${String.format("%20s",owner)}");
            } else {
                if (rootDir(fsima.fileName)!=rootDir(chains[whichChain][-1].fileName)){
                    println("Should not have added ");
                    println("      ${fsima}");
                    println("  to: ${chains[whichChain][-1]}");
                }
            }
            chains[whichChain] << fsima;
            lastImageNumberForChain[whichChain] = imageNumber;
        }
        chains.each(){ chain -> //
            def fsima;
            fsima = chain[0];
            println("Chain of length: ${chain.size()}}");
            println("  From : ${new File(fsima.fileName).getName()} : ${DateFormat.format(fsima.taken)} ${Exif.getCameraOwner(new File(fsima.fileName))}");
            fsima = chain[-1];
            println("    To : ${new File(fsima.fileName).getName()} : ${DateFormat.format(fsima.taken)} ${Exif.getCameraOwner(new File(fsima.fileName))}");
        }
        println("Total of ${chains.size()} chains");
    }

    private void showHisto(List list,Closure mapper){
        Map cloMap = list.groupBy(mapper);
        TreeMap sortedMap = new TreeMap(cloMap);
        sortedMap.each() { mappedval,coll -> //
            println("${String.format("%6d",coll.size())} : ${mappedval} ");
        }
        println("${String.format("%6d",list.size())} : Total ");
    }
    private List getList(){
        Map dbMapByFileName = getMapByFileName();
        List list = [];
        dbMapByFileName.each() { fileName,fsima -> //
            list << fsima;
        }
        list.sort(){ fsima -> //
            fsima.taken.getTime();
        }
        list.each(){ fsima -> //
            //println(DateFormat.format(fsima.taken));
        };
        return list;
    }
    private Map getMapByFileName(){
        Database db = new Database();
        FSImageDAO fsImageDAO = new FSImageDAO();
        fsImageDAO.setDatabase(db);
        Map dbMapByFileName = fsImageDAO.getMapByPrimaryKey();
        db.close();
        return dbMapByFileName;
    }

}
	