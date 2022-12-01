package core;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_dnn_superres.DnnSuperResImpl;

import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import java.io.File;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import javax.swing.ImageIcon;
import java.util.concurrent.Callable;

class Console {
	public static void Log(String str){
			System.out.println(str);
	}
}
@Command(name="test", description = "Helps upscale", mixinStandardHelpOptions = true, version = "1.0")
@SuppressWarnings({"java:S106","java:S2093"})
public class Upscale implements Callable<String>  {
 
		@Option(names = "-mt", description = "modelType")
		private String modelType;
		@Option(names = "-mn", description = "modelName")
		private String modelName;
		@Option(names = "-scale", description = "scale")
		private Integer scale;
		@Option(names = "-loadPath", description = "loadPath")
		private String loadPath;
		@Option(names = "-savePath", description = "savePath")
		private String savePath;


    private Upscale(){}

		public String call() throws Exception {
			run();
			return "";
		}

    public boolean run() {
        ImageIcon s = new ImageIcon(loadPath);
        int width = s.getIconWidth(); 
        int height = s.getIconHeight();
        String originalSize="["+width+"x"+height+"]";      
        width *= Integer.valueOf(scale);
        height *= Integer.valueOf(scale);
        String newSize = "["+width+"x"+height+"]";
        if(width > 6666 || height > 6666) {
            Console.Log("ERROR: Expected output has a side thats bigger than 6666 pixels");
            return false;
        }
				if (loadPath == null) {
					Console.Log("Should pass loadPath");
					return false;
				}
        //no savePath option
        if(savePath == null) {
            StringBuilder sb = new StringBuilder(loadPath);
            savePath = sb.insert(sb.lastIndexOf("."),"("+modelName+")").toString();
        }

				Console.Log("Loading Image");
        Mat image = imread(loadPath);
        if (image.empty()) {
						Console.Log("Error Loading Image");
            return false;
        }
        String modelNamePath = "Models/"+modelName+".pb";
        Mat imageNew = new Mat();
        Console.Log("Loading AI " + modelNamePath);
        DnnSuperResImpl sr = null;
            try {
                sr = new DnnSuperResImpl();
								String modelPathTemp = new File(Upscale.class.getProtectionDomain().getCodeSource().getLocation()
								.toURI().getPath()).getParent()+File.separator+modelNamePath;
								Console.Log("Loading modelPathTemp " + modelPathTemp);

								File modelPath = new File(modelPathTemp);
								if (!modelPath.exists()) {
										Console.Log("Model not found!");
										return false;
								}
                Console.Log("Trying to read model from "+modelPath);
                sr.readModel(modelPath.toString());
                sr.setModel(modelType, Integer.valueOf(scale));
                Console.Log("Algorithm and Size Checked"+"\n \t Starting conversion");
                sr.upsample(image, imageNew);
                
                if(imageNew.isNull()){
								  	Console.Log("Error UpScaling !");
                    return false;
                }          
                Console.Log("Image was successfully upScaled from "+originalSize+"x"+scale+", to "+newSize+"and saved to:");
                Console.Log(savePath);
                imwrite(savePath, imageNew);
                return true;
            } catch(Exception e) {
                Console.Log("Error UpScaling !");
                e.printStackTrace();
                return false;
            }
                finally {
                    imageNew.close();
                    sr.deallocate();
                    sr.close();
            }

    }
		public static void main(String[] args) {
			int rc = new CommandLine(new Upscale()).execute(args);
			System.exit(rc);
		}
}



