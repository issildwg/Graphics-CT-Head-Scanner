import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;


public class Example extends Application {
    short[][][] cthead;
    short min, max;
    int CT_x_axis = 256;
    int CT_y_axis = 256;
    int CT_z_axis = 113;
    boolean renderImage = false;

    /**
     * Deals with the buttons, the sliders and the stage (ie all the visual aspects of the program)
     * @param stage The popup window that displays the Images
     * @throws FileNotFoundException    Checks that there is a file input and throws an error is there isn't
     * @throws IOException  Checks that there is a file input and throws an error is there isn't
     */
    @Override
    public void start(Stage stage) throws FileNotFoundException, IOException {
        stage.setTitle("CThead Viewer");
        ReadData();

        int Top_width = CT_x_axis;
        int Top_height = CT_y_axis;

        int Front_width = CT_x_axis;
        int Front_height = CT_z_axis;

        int Side_width = CT_y_axis;
        int Side_height = CT_z_axis;

        WritableImage top_image = new WritableImage(Top_width, Top_height);
        WritableImage front_image = new WritableImage(Front_width, Front_height);
        WritableImage side_image = new WritableImage(Side_width, Side_height);

        ImageView TopView = new ImageView(top_image);
        ImageView FrontView = new ImageView(front_image);
        ImageView SideView = new ImageView(side_image);

        Button View_button=new Button("Show slice");
        Button Rend_button=new Button("Render");

        Slider Top_slider = new Slider(0, CT_z_axis-1, 0);
        Slider Front_slider = new Slider(0, CT_y_axis-1, 0);
        Slider Side_slider = new Slider(0, CT_x_axis-1, 0);
        Slider Op_slider = new Slider(0, 100, 12);

        View_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //renderImage = !renderImage;       this gets rid of the need for the show slice button
                TopDownSlice(top_image, (int) Top_slider.getValue(), (int) Op_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue(), (int) Op_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue(), (int) Op_slider.getValue());
            }
        });

        Rend_button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                renderImage = !renderImage;
                TopDownSlice(top_image, (int) Top_slider.getValue(), (int) Op_slider.getValue());
                FrontSlice(front_image, (int) Front_slider.getValue(), (int) Op_slider.getValue());
                SideSlice(side_image, (int) Side_slider.getValue(), (int) Op_slider.getValue());
            }
        });

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
     * @throws IOException  Checks that there is an input and throws an error is there isn't
     */
    public void ReadData() throws IOException {
        File file = new File("CThead");
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i;
        int j;
        int k;

        min=Short.MAX_VALUE; max=Short.MIN_VALUE;
        short read;
        int b1, b2;

        cthead = new short[CT_z_axis][CT_y_axis][CT_x_axis];
        for (k=0; k<CT_z_axis; k++) {
            for (j=0; j<CT_y_axis; j++) {
                for (i=0; i<CT_x_axis; i++) {
                    b1=((int)in.readByte()) & 0xff;
                    b2=((int)in.readByte()) & 0xff;
                    read=(short)((b2<<8) | b1);
                    if (read<min) min=read;
                    if (read>max) max=read;
                    cthead[k][j][i]=read;
                }
            }
        }
        System.out.println(min+" "+max);
    }


    /**
     * This function shows how to carry out an operation on an image for the top down view
     * @param image     Reads in the voxels in the Top down direction
     * @param slice     Reads in the slice input by the slice slider
     * @param opacityVal    Reads in the opacity input by the opacity slider
     */
    public void TopDownSlice(WritableImage image, int slice, float opacityVal) {
        int w=(int) image.getWidth(), h=(int) image.getHeight();
        PixelWriter image_writer = image.getPixelWriter();
        float op;
        short datum;

        for (int j=0; j<h; j++) {
            for (int i=0; i<w; i++) {
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
                        Color voxel = tF(cthead[a][j][i], opacityVal);
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
     * @param image     Reads in the voxels in the Front direction
     * @param slice     Reads in the slice input by the slice slider
     * @param opacityVal    Reads in the opacity input by the opacity slider
     */
    public void FrontSlice(WritableImage image, int slice, float opacityVal) {
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
     * @param image     Reads in the voxels in the Side direction
     * @param slice     Reads in the slice input by the slice slider
     * @param opacityVal    Reads in the opacity input by the opacity slider
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
     * Sets the colours and opacities for the volume rendering (renders the skin and bone)
     * @param data  The voxel passed in by the ...Slice methods
     * @param opacityVal    The opacity value passed in by the ...Slice methods
     * @return
     */
    private Color tF (double data, double opacityVal){
        if (data < -300) {
            return Color.color(0, 0, 0, 0);
        } else if (data <= 49) {
            return Color.color(1.0, 0.79, 0.6, opacityVal / 100);
        } else if (data <= 299) {
            return Color.color(0, 0, 0, 0);
        } else if (data <= 4096) {
            return Color.color(1.0, 1.0, 1.0, 0.8);
        }
        return Color.color(0, 0, 0, 0);
    }

    /**
     * this function stops the opacity from surpassing one when volume rendering
     * @param a The opacity value passed in by the ...Slice methods
     * @return
     */
    public double clampNumber(double a){
        if(a > 1.0) {
            return 1.0;
        } else {
            return a;
        }
    }

    /**
     * Main method which launches the program
     * @param args  arguments
     */
    public static void main(String[] args) {
        launch();
    }
}