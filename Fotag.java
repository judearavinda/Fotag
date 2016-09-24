import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.DimensionUIResource;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jude on 3/12/2016.
 */
public class Fotag {

    public class MainFrame extends JFrame implements Observer,ComponentListener {
        private ImageCollectionModel model;
        private ImageCollectionView mainPanel;
        JPanel viewPanel;
        private JScrollPane mainPanelContainer;

        // constructor, create the GUI. This is the view
        public MainFrame(ImageCollectionModel newModel) {
            // set the model and create frame
            super("Fotag");
            model = newModel;
            viewPanel = new JPanel(new GridBagLayout());
            this.setMinimumSize(new DimensionUIResource(700, 700));

            mainPanel = new ImageCollectionView(newModel);
            newModel.addObserver(mainPanel);
            mainPanelContainer = new JScrollPane(mainPanel,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainPanelContainer.getVerticalScrollBar().setUnitIncrement(10);
            this.pack();
            this.setVisible(true);
            this.addComponentListener(this);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            this.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    if (model.saveImages())
                    {
                        JOptionPane.showMessageDialog(MainFrame.this, "Images were saved successfuly");
                        System.exit(0);
                    }
                    else{
                        JOptionPane.showMessageDialog(MainFrame.this, "Saving was unsuccessful.\n\rPlease try again.");
                        System.exit(0);
                    }
                }
            });

            // Setup Main Frame
            this.getContentPane().setLayout(new BorderLayout());
            //main toolbar
            JToolBar toolBar = new JToolBar();
            toolBar.setLayout(new BorderLayout());
            Panel rightPanel = new Panel();
            rightPanel.setLayout(new GridLayout());
            Panel leftPanel = new Panel();
            leftPanel.setLayout(new GridLayout());
            JToggleButton gridButton = new JToggleButton();
            JToggleButton listButton = new JToggleButton();
            JButton loadButton = new JButton();
            //set images on the buttons
            gridButton.setIcon(new ImageIcon(getClass().getResource("/resources/gridlayout.png")));
            gridButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    model.setCurrentLayout(LayoutType.Grid);
                    listButton.setSelected(false);
                }
            });
            listButton.setIcon(new ImageIcon((getClass().getResource("/resources/listlayout.png"))));
            listButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    model.setCurrentLayout(LayoutType.List);
                    gridButton.setSelected(false);
                }
            });
            loadButton.setIcon(new ImageIcon((getClass().getResource("/resources/load.png"))));
            loadButton.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    model.loadImage(null);
                }
            });
            //set the JLabel
            JLabel titleLabel = new JLabel("Fotag!", SwingConstants.CENTER);
            titleLabel.setFont(new Font(titleLabel.getName(), Font.PLAIN, 50));
            //set the sizes of the buttons
            gridButton.setPreferredSize(new Dimension(64, 64));
            listButton.setPreferredSize(new Dimension(64, 64));
            loadButton.setPreferredSize(new Dimension(64, 64));
            //set the reviewPanel
            FilterToolBar reviewPanel = new FilterToolBar(model);
            rightPanel.add(gridButton);
            rightPanel.add(listButton);
            leftPanel.add(loadButton);
            leftPanel.add(reviewPanel);
            toolBar.add(rightPanel, BorderLayout.LINE_START);
            toolBar.add(titleLabel, BorderLayout.CENTER);
            toolBar.add(leftPanel, BorderLayout.LINE_END);
            this.add(toolBar, BorderLayout.PAGE_START);
            this.getContentPane().add(mainPanelContainer);
        }

        @Override
        public void update(Observable o, Object arg) {
            System.out.println("Resized!!");
            resize();
        }
        @Override
        public void componentResized(ComponentEvent e) {
            System.out.println("Frame was resized");
            resize();
        }
        @Override
        public void componentMoved(ComponentEvent e) {}

        @Override
        public void componentShown(ComponentEvent e) {}

        @Override
        public void componentHidden(ComponentEvent e) {}

        public void resize()
        {
            int viewW = 200;
            int viewH = 271;
            if (model.getCurrentLayout() == LayoutType.Grid){
                int w = (int)mainPanelContainer.getSize().getWidth();
                int h = 100000;

                h = w/viewW;
                h = (int)Math.ceil(mainPanel.getImageViews().size()/(float)h);
                h = h * viewH;
                mainPanel.setMaximumSize(new Dimension(w, h));
                mainPanel.setPreferredSize(new Dimension(w, h));
            }
            else{
                mainPanel.setPreferredSize(null);
                mainPanel.setMaximumSize(null);
                mainPanel.setMinimumSize(null);
            }
            mainPanelContainer.revalidate();
            mainPanel.revalidate();
            revalidate();
        }
    }

    public class FilterToolBar extends FiveStarReviewPanel implements Observer{
        public FilterToolBar(ImageCollectionModel newModel)
        {
            super(null);
            this.rating= false;
            this.model = newModel;
            filter = true;
        }

        @Override
        public void update(Observable o, Object arg) {
            RedrawStars(model.getFilter());
        }
    }
    public class ImageModel extends Observable implements Serializable {
        private String path;
        private String filename;
        private String fileCreationDate;
        private Integer rating;

        public ImageModel(String path, String filename, String fileCreationDate)
        {
            this.path = path;
            this.filename = filename;
            this.fileCreationDate =fileCreationDate;
            setRating(0);
            this.setChanged();
        }
        public String getFilename() {
            return filename;
        }

        public String getPath() {
            return path;
        }

        public String getFileCreationDate() {
            return fileCreationDate;
        }
        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
            setChanged();
            notifyObservers();
        }
    }
        public class FiveStarReviewPanel extends JPanel implements Observer,Serializable{
            JLabel Star1;
            JLabel Star2;
            JLabel Star3;
            JLabel Star4;
            JLabel Star5;
            boolean filter = false;
            ImageIcon yellowStar;
            ImageIcon emptyStar;
            int currentRating;
            ImageCollectionModel model;
            ImageModel imageModel;
            boolean rating = false;

            public void setCurrentRating(int currentRating) {
                this.currentRating = currentRating;
            }

            public int getCurrentRating() {
                return currentRating;
            }
            public FiveStarReviewPanel(ImageModel model)
            {
                this.imageModel = model;
                this.rating = true;
                //set the Icons
                emptyStar =  new ImageIcon(new ImageIcon(getClass().getResource("/resources/whiteStar.png")).getImage().getScaledInstance(32,32,Image.SCALE_SMOOTH));
                yellowStar =  new ImageIcon(new ImageIcon(getClass().getResource("/resources/yellowStar.png")).getImage().getScaledInstance(32,32,Image.SCALE_SMOOTH));
                //set the border
                Border blackline = BorderFactory.createLineBorder(Color.black);
                //initialize all five star review icons
                this.setLayout(new GridLayout());
                Star1 = new JLabel();
                Star2 = new JLabel();
                Star3 = new JLabel();
                Star4 = new JLabel();
                Star5 = new JLabel();
                InitializeStars();
                Star1.setIcon(emptyStar);
                Star2.setIcon(emptyStar);
                Star3.setIcon(emptyStar);
                Star4.setIcon(emptyStar);
                Star5.setIcon(emptyStar);
                this.add(Star1);
                this.add(Star2);
                this.add(Star3);
                this.add(Star4);
                this.add(Star5);
                this.setBorder(blackline);
                repaint();
            }
            public void RedrawStars(int rating)
            {
                ResetStars();
                JLabel[] starArray = {Star1,Star2,Star3,Star4,Star5};
                for (int i = 0;i<rating;i++)
                {
                    starArray[i].setIcon(yellowStar);
                }
                repaint();
            }
            public void InitializeStars()
            {
                //attach mouselisteners to the labels

                Star1.addMouseListener(new MouseAdapter()
                {
                    public void mousePressed(MouseEvent evt)
                    {
                        if (currentRating!=1)
                        {
                            ResetStars();
                            Star1.setIcon(yellowStar);
                            setCurrentRating(1);
                            if (filter == true)
                            {
                                model.setFilter(1);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(1);
                            }
                        }
                        else
                        {
                            setCurrentRating(0);
                            ResetStars();
                            if (filter == true)
                            {
                                model.setFilter(0);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(0);
                            }
                        }
                        repaint();
                        System.out.println(getCurrentRating());
                    }

                });
                Star2.addMouseListener(new MouseAdapter()
                {
                    public void mousePressed(MouseEvent evt)
                    {
                        if (currentRating!=2)
                        {
                            ResetStars();
                            Star2.setIcon(yellowStar);
                            Star1.setIcon(yellowStar);
                            setCurrentRating(2);
                            if (filter == true)
                            {
                                model.setFilter(2);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(2);
                            }
                        }
                        else
                        {
                            setCurrentRating(0);
                            ResetStars();
                            if (filter == true)
                            {
                                model.setFilter(0);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(0);
                            }
                        }
                        repaint();
                        System.out.println(getCurrentRating());
                    }

                });
                Star3.addMouseListener(new MouseAdapter()
                {
                    public void mousePressed(MouseEvent evt)
                    {
                        if (currentRating!=3)
                        {
                            ResetStars();
                            Star3.setIcon(yellowStar);
                            Star2.setIcon(yellowStar);
                            Star1.setIcon(yellowStar);
                            setCurrentRating(3);
                            if (filter == true)
                            {
                                model.setFilter(3);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(3);
                            }
                        }
                        else
                        {
                            setCurrentRating(0);
                            ResetStars();
                            if (filter == true)
                            {
                                model.setFilter(0);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(0);
                            }
                        }
                        repaint();
                        System.out.println(getCurrentRating());
                    }

                });
                Star4.addMouseListener(new MouseAdapter()
                {
                    public void mousePressed(MouseEvent evt)
                    {
                        if (currentRating!=4)
                        {
                            ResetStars();
                            Star4.setIcon(yellowStar);
                            Star3.setIcon(yellowStar);
                            Star2.setIcon(yellowStar);
                            Star1.setIcon(yellowStar);
                            setCurrentRating(4);
                            if (filter == true)
                            {
                                model.setFilter(4);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(4);
                            }
                        }
                        else
                        {
                            setCurrentRating(0);
                            ResetStars();
                            if (filter == true)
                            {
                                model.setFilter(0);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(0);
                            }
                        }
                        repaint();
                        System.out.println(getCurrentRating());
                    }

                });
                Star5.addMouseListener(new MouseAdapter()
                {
                    public void mousePressed(MouseEvent evt)
                    {
                        if (currentRating!=5)
                        {
                            ResetStars();
                            Star5.setIcon(yellowStar);
                            Star4.setIcon(yellowStar);
                            Star3.setIcon(yellowStar);
                            Star2.setIcon(yellowStar);
                            Star1.setIcon(yellowStar);
                            setCurrentRating(5);
                            if (filter == true)
                            {
                                model.setFilter(5);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(5);
                            }
                        }
                        else
                        {
                            setCurrentRating(0);
                            ResetStars();
                            if (filter == true)
                            {
                                model.setFilter(0);
                            }
                            if (rating == true)
                            {
                                imageModel.setRating(0);
                            }
                        }
                        repaint();
                        System.out.println(getCurrentRating());
                    }

                });
            }
            public void ResetStars()
            {
                Star1.setIcon(emptyStar);
                Star2.setIcon(emptyStar);
                Star3.setIcon(emptyStar);
                Star4.setIcon(emptyStar);
                Star5.setIcon(emptyStar);
            }
            public Dimension getPreferredSize()
            {
                return new Dimension(160,40);
            }

            @Override
            public void update(Observable o, Object arg) {
                RedrawStars(this.getCurrentRating());
            }
        }
    public class ImageView extends JPanel implements Observer,Serializable{

        private ImageModel imageModel;
        private FiveStarReviewPanel rating;
        private JLabel name;
        private JLabel createdDate;
        LayoutManager listLayout,gridLayout;
        private JLabel thumbnail;

        int viewW = 200;
        int viewH = 271;

        public ImageView(ImageModel model)
        {
            this.imageModel = model;
            rating = new FiveStarReviewPanel(model);
            rating.setCurrentRating(model.getRating());

            this.imageModel.addObserver(rating);
            listLayout = new BoxLayout(this, BoxLayout.X_AXIS);
            gridLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
            this.setLayout(listLayout);
            Image img;
            //set the thumbnail
            try {
                img = ImageIO.read(new File(imageModel.getPath())).getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);
            } catch (IOException e) {
                e.printStackTrace();
                img = null;
            }
            thumbnail = new JLabel( new ImageIcon(Utilities.scaleWithAspectRatio(img, viewW, 200)) );
            name = new JLabel("Name : " +imageModel.getFilename());
            createdDate = new JLabel("Date Created : " + imageModel.getFileCreationDate());

            JPanel PictureContent = new JPanel();
            PictureContent.setLayout(new BoxLayout(PictureContent, BoxLayout.Y_AXIS));
            PictureContent.add(name);
            PictureContent.add(createdDate);
            PictureContent.add(rating);

            this.add(thumbnail, BorderLayout.PAGE_START);
            this.add(PictureContent, BorderLayout.PAGE_START);

            thumbnail.setAlignmentX(LEFT_ALIGNMENT);
            name.setAlignmentX(LEFT_ALIGNMENT);
            rating.setAlignmentX(LEFT_ALIGNMENT);
            thumbnail.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    new ImageDialog(new File(imageModel.getPath()), model.getFilename());
                }
            });
        }
        @Override
        public void update(Observable o, Object arg) {
            rating.update(o,arg);
            System.out.println("rating is now" + imageModel.getRating());
        }

        public void changeLayout(LayoutType newLayout){
            if (newLayout == LayoutType.List){
                this.setLayout(listLayout);
                this.setMaximumSize(new Dimension(100000, 200));
            }
            else if (newLayout == LayoutType.Grid){
                this.setLayout(gridLayout);
            }
            else{
                System.out.println("Something is wrong");
            }
            repaint();
            revalidate();
        }

        public void UpdateVisibility(Integer newFilterVal){
            Integer rating = imageModel.getRating();
            if (newFilterVal == null){
                this.setVisible(true);
            }
            else if (rating == null){
                this.setVisible(false);
            }
            else if(rating < newFilterVal){
                this.setVisible(false);
            }
            else{
                this.setVisible(true);
            }
            repaint();
            revalidate();
        }
    }
    public class ImageCollectionView extends JPanel implements Observer,Serializable  {
        private ImageCollectionModel imagesModel;
        private ArrayList<ImageView> imageViews;

        public ArrayList<ImageView> getImageViews() {
            return imageViews;
        }

        public void setImageViews(ArrayList<ImageView> imageViews) {
            this.imageViews = imageViews;
        }

        public ImageCollectionView(ImageCollectionModel imageCollection)
        {
            super();
            this.setBackground(new Color(100, 100, 255));
            this.imagesModel = imageCollection;
            this.imageViews = new ArrayList<ImageView>();
        }
        @Override
        public void update(Observable o, Object arg) {
            System.out.println("ImageCollectionView update() called");
            ImageModel newImage = imagesModel.getNewImage();
            if (newImage!=null)
            {
                //initialize everything
                ImageView newImageView = new ImageView(newImage);
                newImage.addObserver(newImageView);
                newImage.notifyObservers();

                imageViews.add(newImageView);
                this.add(newImageView);
                revalidate();
            }
            //change layout for all the images
            for(ImageView image : imageViews){
                image.changeLayout(imagesModel.getCurrentLayout());
                image.UpdateVisibility(imagesModel.getFilter());
            }

            if (imagesModel.getCurrentLayout() == LayoutType.List) {
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            }

            else if (imagesModel.getCurrentLayout() == LayoutType.Grid){
                this.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            }
            this.revalidate();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }

        private void onRate(ImageView img) {
            img.UpdateVisibility(imagesModel.getFilter());
        }

    }
    public enum LayoutType
    {Grid,List};
    public class ImageCollectionModel extends Observable implements Serializable {
        LayoutType currentLayout;
        int Filter;
        private ArrayList<ImageModel> imageModels;
        ImageModel newImage;

        public void setImagesView(ArrayList<ImageView> imagesView) {
            setChanged();
            notifyObservers();
        }

        public ImageModel getNewImage() {
            return newImage;
        }

        public LayoutType getCurrentLayout() {
            return currentLayout;
        }

        public int getFilter() {
            return Filter;
        }

        public void setFilter(int filter) {
            Filter = filter;
            setChanged();
            notifyObservers();
        }

        public ImageCollectionModel()
        {
            currentLayout = LayoutType.List;
            imageModels = new ArrayList<ImageModel>();
            loadImages();
            newImage = null;
            setChanged();
        }

        public void setCurrentLayout(LayoutType currentLayout) {
            this.currentLayout = currentLayout;
            setChanged();
            notifyObservers();
        }

        public void loadSavedPics(){
            for(int i = 0; i < imageModels.size(); i++){
                ImageModel currentModel = imageModels.get(i);
                displayImageModel(currentModel);
                currentModel.setRating(currentModel.getRating());
                currentModel.notifyObservers();
            }
        }

        public ArrayList<ImageModel> getImageModels() {
            return imageModels;
        }

        public void setImageModels(ArrayList<ImageModel> imageModels) {
            this.imageModels = imageModels;
            setChanged();
            notifyObservers();
        }

        public void addImage(ImageModel newImage)
        {
            String last = newImage.getPath();
            //Scan image models to see if image already exists
            for(ImageModel image: imageModels){
                String first = image.getPath();
                if (first.equals(last)){
                    JOptionPane.showMessageDialog(null, "Selected image is already in Fotag!", "Choose a valid Image!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            addPicture(newImage);
        }
        public void addPicture(ImageModel newImage)
        {
            imageModels.add(newImage);
            displayImageModel(newImage);
        }
        public void displayImageModel(ImageModel Pic){
            newImage = Pic;
            setChanged();
            notifyObservers();
            newImage = null;
        }

        public boolean loadImages(){
            try{
                FileInputStream fileIn = new FileInputStream("savedImages.txt");
                ObjectInputStream in = new ObjectInputStream(fileIn);

                ImageCollectionModel temp = (ImageCollectionModel) in.readObject();
                this.imageModels = temp.imageModels;
                in.close();
                fileIn.close();
                return true;
            }
            catch(Exception e){
                System.out.println("No valid file : " + e);
                return false;
            }
        }

        public boolean saveImages(){
            try{
                File saveFile = new File("savedImages.txt");
                if(!saveFile.exists()) {
                    saveFile.createNewFile();
                }
                FileOutputStream fileOut = new FileOutputStream(saveFile,false);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(this.getImageModels().get(0));
                out.close();
                fileOut.close();
                return true;
            }
            catch(Exception e){
                System.out.println("No valid file : " + e);
                return false;
            }
        }


        public void loadImage(JFrame f)
        {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter typeJPG = new FileNameExtensionFilter("JPEG Images", "jpg");
            FileNameExtensionFilter typePNG = new FileNameExtensionFilter("PNG Images", "png");
            FileNameExtensionFilter typeGIF = new FileNameExtensionFilter("GIF Images", "gif");
            fileChooser.addChoosableFileFilter(typeJPG);
            fileChooser.addChoosableFileFilter(typePNG);
            fileChooser.addChoosableFileFilter(typeGIF);
            int returnValue = fileChooser.showOpenDialog(f);
            if (returnValue == JFileChooser.APPROVE_OPTION)
            {
                File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                if (path.endsWith(".jpg")||path.endsWith(".jpeg")||path.endsWith(".bmp")||path.endsWith(".gif")||path.endsWith(".png"))
                {
                    //valid image
                    try {
                        //create a new imageModel
                        Path p = Paths.get(path);
                        BasicFileAttributes attr = Files.getFileAttributeView(p,BasicFileAttributeView.class).readAttributes();
                        FileTime time = attr.creationTime();
                        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                        ImageModel newImageModel = new ImageModel(path,p.getFileName().toString(),df.format(time.toMillis()));
                        addImage(newImageModel);
                        System.out.println(getImageModels().size());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    //invalid image
                    JOptionPane.showMessageDialog(f, "You must choose a valid image!", "Invalid File Format", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    public static class Utilities{
        public static Image scaleWithAspectRatio(Image srcImg, int w, int h){
            int srcH = srcImg.getHeight(null);
            int srcW = srcImg.getWidth(null);

            BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if ( ((float)srcH)/srcW < ((float)h)/w)
                g2.drawImage(srcImg, 0, (h - srcH*w/srcW)/2, w, srcH*w/srcW, null);
            else
                g2.drawImage(srcImg, (w - srcW*h/srcH)/2, 0, srcW*h/srcH, h, null);
            g2.dispose();
            return resizedImg;

        }
    }
    public class ImageDialog extends JFrame{
        public ImageDialog(File f, String fileName){
            super(fileName);

            JPanel p = new JPanel();
            this.getContentPane().add(p);

            Image img;
            try{
                img = ImageIO.read(f);
            }
            catch (Exception e){
                img = null;
            }

//        p.add(new JLabel( new ImageIcon(Utilities.getScaledImage(img, 500, 500)) ));
            p.add(new JLabel( new ImageIcon(Utilities.scaleWithAspectRatio(img, 500, 500)) ));

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE );

            this.pack();
            this.setVisible(true);
        }
    }
    public void launchFrame() {
        ImageCollectionModel model = new ImageCollectionModel();
        MainFrame mainFrame = new MainFrame(model);
        ImageCollectionView imageViews = new ImageCollectionView(model);
        model.addObserver(imageViews);
        mainFrame.setSize(new Dimension(800, 600));
        mainFrame.setVisible(true);
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

        } catch (Exception e) {
            System.err.println("Look and feel not set.");
        }

        // build
        Fotag demo = new Fotag();
        demo.launchFrame();
    }
}
