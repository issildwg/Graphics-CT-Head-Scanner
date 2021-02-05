import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.sun.deploy.uitoolkit.ui.ConsoleTraceListener;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;


public class Example extends Application {
    short cthead[][][]; //store the 3D volume data set
    short min, max; //min/max value in the 3D volume data set
    int CT_x_axis = 256;
    int CT_y_axis = 256;
    int CT_z_axis = 113;
    boolean renderImage = false;

    /**
     * Deals with the buttons, the sliders and the stage (ie all the visual aspects of the program)
     * @param stage
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");


        ReadData();

        //Top view - goes through z axis
        int Top_width = CT_x_axis;
        int Top_height = CT_y_axis;

        //front view - goes through y axis
        int Front_width = CT_x_axis;
        int Front_height = CT_z_axis;

        //side view - goes through x axis
        int Side_width = CT_y_axis;
        int Side_height = CT_z_axis;


        //creates an image (we can write to)
        WritableImage top_image = new WritableImage(Top_width, Top_height);
        WritableImage front_image = new WritableImage(Front_width, Front_height);
        WritableImage side_image = new WritableImage(Side_width, Side_height);

        //creates a view of those images above
        ImageView TopView = new ImageView(top_image);
        ImageView FrontView = new ImageView(front_image);
        ImageView SideView = new ImageView(side_image);


        //buttons
        Button View_button=new Button("Show slice");
        Button Rend_button=new Button("Render");

        //sliders
        Slider Top_slider = new Slider(0, CT_z_axis-1, 0);
        Slider Front_slider = new Slider(0, CT_y_axis-1, 0);
        Slider Side_slider = new Slider(0, CT_x_axis-1, 0);
        Slider Op_slider = new Slider(0, 100, 12);

        //gets the image when the View button is pressed
        View_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //renderImage = !renderImage;       this gets rid of the need for the show slice button
                TopDownSlice(top_image, (int) Top_slider.getValue(), (int) Op_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue(), (int) Op_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue(), (int) Op_slider.getValue());
            }
        });

        //toggles having a coloured image when the Render button is pressed
        Rend_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                renderImage = !renderImage;
                TopDownSlice(top_image, (int) Top_slider.getValue(), (int) Op_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue(), (int) Op_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue(), (int) Op_slider.getValue());
            }
        });


        // listens to the number on the slice sliders and displays them
        Top_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                TopDownSlice(top_image, (int) Top_slider.getValue(), (int) Op_slider.getValue());
            }
        });

        Front_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                FrontSlice(front_image, (int) Front_slider.getValue(), (int) Op_slider.getValue());
            }
        });

        Side_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                SideSlice(side_image, (int) Side_slider.getValue(), (int) Op_slider.getValue());
            }
        });

        // listens to the number on the opacity slider and displays it
        Op_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                TopDownSlice(top_image, (int) Top_slider.getValue(), (int) Op_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue(), (int) Op_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue(), (int) Op_slider.getValue());
            }
        });

        FlowPane root = new FlowPane();
        root.setVgap(25);
        root.setHgap(10);


        //adds things to the flow pane
        root.getChildren().addAll(TopView, Top_slider);
        root.getChildren().addAll(FrontView, Front_slider);
        root.getChildren().addAll(SideView, Side_slider);
        root.getChildren().addAll(View_button, Rend_button, Op_slider);

        Scene scene = new Scene(root, 425,600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Function to read in the cthead data set
     * @throws IOException
     */
    public void ReadData() throws IOException {
        File file = new File("CThead");
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i;
        int j;
        int k; //loop through the 3D data set

        min=Short.MAX_VALUE; max=Short.MIN_VALUE;
        short read; //value read in
        int b1, b2; //data is wrong Endian (order of bytes) for Java so we need to swap the bytes around

        cthead = new short[CT_z_axis][CT_y_axis][CT_x_axis]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k=0; k<CT_z_axis; k++) {
            for (j=0; j<CT_y_axis; j++) {
                for (i=0; i<CT_x_axis; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types
                    read=(short)((b2<<8) | b1); //and swizzle the bytes around
                    if (read<min) min=read; //update the minimum
                    if (read>max) max=read; //update the maximum
                    cthead[k][j][i]=read; //put the short into memory
                }
            }
        }
        System.out.println(min+" "+max);
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
        //maybe put your histogram equalization code here to set up the mapping array
    }


    //  these functions obtain the dimensions of the image, and then loop through the image carrying out the copying of a slice of data into the image.
    /**
     * This function shows how to carry out an operation on an image for the top down view
     * @param image
     * @param slice
     * @param opacityVal
     */
    public void TopDownSlice(WritableImage image, int slice, float opacityVal) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        float op;
        short datum;
                //Shows how to loop through each pixel and colour
                //Try to always use j for loops in y, and i for loops in x as this makes the code more readable
        for (int j=0; j<h; j++) {  // row loop
            for (int i=0; i<w; i++) { // column loop
                        /* at this point (i,j) is a single pixel in the image here you would need to do something to (i,j) if the image size does not match the slice size (e.g. during an image resizing operation
                        If you don't do this, your j,i could be outside the array bounds
                        In the framework, the image is 256x256 and the data set slices are 256x256 so I don't do anything - this also leaves you something to do for the assignment */

                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour

                if(!renderImage){
                    datum = cthead[slice][j][i];
                    double col = (((float) datum - (float) min) / ((float) (max - min)));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

                }else{
                    double r = 0;
                    double g = 0;
                    double b = 0;
                    double transparency = 1.0;

                    for (int a = slice; a < 113; a++) {
                        Color voxel = tF(cthead[a][j][i], opacityVal);                      //adjust to slice - from datum...
                        //from the lecture slides
                        r += voxel.getRed() * voxel.getOpacity() * transparency;
                        g += voxel.getGreen() * voxel.getOpacity() * transparency;
                        b += voxel.getBlue() * voxel.getOpacity() * transparency;
                        transparency *= (1.0 - voxel.getOpacity());
                    }
                    image_writer.setColor(i, j, new Color(clampNumber(r), clampNumber(g), clampNumber(b), 1));
                }
            }
        }
    }


    /**
     * This function shows how to carry out an operation on an image for the front facing view
     * @param image
     * @param slice
     * @param opacityVal
     */
    public void FrontSlice(WritableImage image, int slice, float opacityVal) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                datum=cthead[j][slice][i];
                col=(((float)datum-(float)min)/((float)(max-min)));
                if(!renderImage){
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

                }else{
                    double r = 0;
                    double g = 0;
                    double b = 0;
                    double transparency = 1.0;

                    for (int a = slice; a < 256; a++) {
                        Color voxel = tF(cthead[j][a][i], opacityVal);
                        r += voxel.getRed() * voxel.getOpacity() * transparency;
                        g += voxel.getGreen() * voxel.getOpacity() * transparency;
                        b += voxel.getBlue() * voxel.getOpacity() * transparency;
                        transparency *= (1.0 - voxel.getOpacity());
                    }
                    image_writer.setColor(i, j, new Color(clampNumber(r), clampNumber(g), clampNumber(b), 1));
                }
            }
        }
    }

    /**
     * This function shows how to carry out an operation on an image for the side facing view
     * @param image
     * @param slice
     * @param opacityVal
     */
    public void SideSlice(WritableImage image, int slice, float opacityVal) {
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                datum=cthead[j][i][slice];
                col=(((float)datum-(float)min)/((float)(max-min)));
                if(!renderImage){
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

                }else{
                    double r = 0;
                    double g = 0;
                    double b = 0;
                    double transparency = 1.0;

                    for (int a = slice; a < 256; a++) {
                        Color voxel = tF(cthead[j][i][a], opacityVal);
                        //from the lecture slides
                        r += voxel.getRed() * voxel.getOpacity() * transparency;
                        g += voxel.getGreen() * voxel.getOpacity() * transparency;
                        b += voxel.getBlue() * voxel.getOpacity() * transparency;
                        transparency *= (1.0 - voxel.getOpacity());
                    }
                    image_writer.setColor(i, j, new Color(clampNumber(r), clampNumber(g), clampNumber(b), 1));
                }
            }
        }
    }

    private Color tF (double data, double opacityVal){
        if (data < -300) {
            return Color.color(0, 0, 0, 0);
        } else if (data <= 49) {
            return Color.color(1.0, 0.79, 0.6, opacityVal / 100);     //SKIN
        } else if (data <= 299) {
            return Color.color(0, 0, 0, 0);
        } else if (data <= 4096) {
            return Color.color(1.0, 1.0, 1.0, 0.8);   //BONE
        }
        return Color.color(0, 0, 0, 0);
    }

    public double clampNumber(double a){
        if(a > 1.0) {
            return 1.0;
        } else {
            return a;
        }
    }

    /**
     * Main method which launches the program
     * @param args
     */
    public static void main(String[] args) {
        launch();
    }
}