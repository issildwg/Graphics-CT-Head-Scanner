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


        // was slice76_button - is now View_button -- press to show slices
        Button View_button=new Button("Show slice");

        Button Rend_button=new Button("Render");

        //sliders to step through the slices
        Slider Top_slider = new Slider(0, CT_z_axis-1, 0);
        Slider Front_slider = new Slider(0, CT_y_axis-1, 0);
        Slider Side_slider = new Slider(0, CT_x_axis-1, 0);
        Slider Rend_slider = new Slider(0, 100, 0);

        //gets the image when the above button is pressed
        View_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //renderImage = !renderImage;       this gets rid of the need for the show slice button
                TopDownSlice(top_image, (int) Top_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue());
            }
        });

        //add colour to image when button is pressed
//TODO do i need the render slider in here
        Rend_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                renderImage = !renderImage;
                //    CTcol(((int) Rend_slider.getValue())/100);
// TODO change these values to the colour render thing
                TopDownSlice(top_image, (int) Top_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue());
            }
        });


        // listens to the number on the slider and displays it
        Top_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                TopDownSlice(top_image, (int) Top_slider.getValue());
            }
        });

        Front_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                FrontSlice(front_image, (int) Front_slider.getValue());
            }
        });

        Side_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
                SideSlice(side_image, (int) Side_slider.getValue());
            }
        });

    //TODO fix this for the render slider
        Rend_slider.valueProperty().addListener( new ChangeListener<Number>() {
            public void changed(ObservableValue <? extends Number > observable, Number oldValue, Number newValue) {
                System.out.println(newValue.intValue());
         //       CTcol(((int) Rend_slider.getValue())/100);
//TODO add the slices here maybe so i can add the colour to them
            }
        });

        FlowPane root = new FlowPane();
        root.setVgap(25);
        root.setHgap(10);


        //adds things to the flow pane
        root.getChildren().addAll(TopView, Top_slider);
        root.getChildren().addAll(FrontView, Front_slider);
        root.getChildren().addAll(SideView, Side_slider);
        root.getChildren().addAll(View_button, Rend_button, Rend_slider);

        Scene scene = new Scene(root, 425,600);
        stage.setScene(scene);
        stage.show();
    }

    //Function to read in the cthead data set
    public void ReadData() throws IOException {
        File file = new File("CThead");
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
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
        System.out.println(min+" "+max); //diagnostic - for CThead this should be -1117, 2248
        //(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
        //therefore histogram equalization would be a good thing
        //maybe put your histogram equalization code here to set up the mapping array
    }




     /*  This function shows how to carry out an operation on an image.
       It obtains the dimensions of the image, and then loops through the image carrying out the copying of a slice of data into the image. */

//READS PIXELS AND WRITES THEM OUT TO A CERTAIN SIZE - CHANGE IT?
//TopDownSlice was TopDownSlice76
    public void TopDownSlice(WritableImage image, int z) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
                //Shows how to loop through each pixel and colour
                //Try to always use j for loops in y, and i for loops in x as this makes the code more readable
        for (int j=0; j<h; j++) {  // row loop
            for (int i=0; i<w; i++) { // column loop
                        /* at this point (i,j) is a single pixel in the image here you would need to do something to (i,j) if the image size does not match the slice size (e.g. during an image resizing operation
                        If you don't do this, your j,i could be outside the array bounds
                        In the framework, the image is 256x256 and the data set slices are 256x256 so I don't do anything - this also leaves you something to do for the assignment */
                datum = cthead[z][j][i];
                //calculate the colour by performing a mapping from [min,max] -> 0 to 1 (float)
                //Java setColor uses float values from 0 to 1 rather than 0-255 bytes for colour
                col = (((float) datum - (float) min) / ((float) (max - min)));
                if(!renderImage){
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                }else{
                    if (datum < -300) {
                        image_writer.setColor(i, j, Color.color(0, 0, 0, 0));
                    } else if (-300 <= datum && datum <= 49) {
                        image_writer.setColor(i, j, Color.color(1.0, 0.79, 0.6, 0.12));
                    } else if (50 <= datum && datum <= 299) {
                        image_writer.setColor(i, j, Color.color(0, 0, 0, 0));
                    } else if (300 <= datum && datum <= 4096) {
                        image_writer.setColor(i, j, Color.color(1.0, 1.0, 1.0, 0.8));
                    }
                }
            }
        }
    }

    public void FrontSlice(WritableImage image, int y) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                datum=cthead[j][y][i];
                col=(((float)datum-(float)min)/((float)(max-min)));
                if(!renderImage){
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                }else {
                    if (datum < -300) {
                        image_writer.setColor(i, j, Color.color(0, 0, 0, 0));
                    } else if (-300 <= datum && datum <= 49) {
                        image_writer.setColor(i, j, Color.color(1.0, 0.79, 0.6, 0.12));
                    } else if (50 <= datum && datum <= 299) {
                        image_writer.setColor(i, j, Color.color(0, 0, 0, 0));
                    } else if (300 <= datum && datum <= 4096) {
                        image_writer.setColor(i, j, Color.color(1.0, 1.0, 1.0, 0.8));
                    }
                }
            }
        }
    }

    public void SideSlice(WritableImage image, int x) {
        //Get image dimensions, and declare loop variables
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();

        double col;
        short datum;
        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
                datum=cthead[j][i][x];
                col=(((float)datum-(float)min)/((float)(max-min)));
                if(!renderImage){
                    image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
                }else {
                    if (datum < -300) {
                        image_writer.setColor(i, j, Color.color(0, 0, 0, 0));
                    } else if (-300 <= datum && datum <= 49) {
                        image_writer.setColor(i, j, Color.color(1.0, 0.79, 0.6, 0.12));
                    } else if (50 <= datum && datum <= 299) {
                        image_writer.setColor(i, j, Color.color(0, 0, 0, 0));
                    } else if (300 <= datum && datum <= 4096) {
                        image_writer.setColor(i, j, Color.color(1.0, 1.0, 1.0, 0.8));
                    }
                }
            }
        }
    }


//MAIN METHOD
    public static void main(String[] args) {
        launch();
    }

}