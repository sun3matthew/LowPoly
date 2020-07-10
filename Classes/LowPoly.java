import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;


/*
java EvoPicture filename

The images (in .jpg format) are added into a folder in the Input_Images,
the two images should be named One.jpg and Two.jpg, the "Two" image acts
as the world and the "One" image acts as the sample population that will
be selected to populate the world.

Random pixels from the the "One" image are selected and put into the world.
For the first 15 generation, each pixel will look at the 8 pixels around it
to find the "Best" home, this goes on for about 40 frames. Then after it the
40 frames, the pixels who are too diffrent from the current world pixel they
are currently on are deleted. The remaining pixels have create children who
are slightly mutated versions of themselves(colorwise). This process goes on
for 15 more generations, then after the 15 generations the pixels will no
longer look/move and just produce more child pixels. Even the majority of
the pixels on the screen is covered, the program will end, exporting the images
into the Output_Images.

By: Matthew Sun
Since: May 31 2020
*/

/*
The main class to setup java GUI
*/
public class LowPoly {
    JFrame frame;
    AiProgram canvas;
    public static void main(String[] args) {
        String folderName = "Lofi";
        if (args.length > 0)
            folderName = args[0];
        LowPoly kt = new LowPoly();
        kt.Run(folderName);
    }

    public void Run(String name) {
        frame = new JFrame("LowPoly");
        frame.setSize(1050, 1050);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        canvas = new AiProgram(name);
        frame.getContentPane().add(canvas);

        frame.setVisible(true);
    }
}

/*
The the class the runs the program
*/
class AiProgram extends JPanel {

    Pixel[][] currentImage;
    Pixel[][] wallLines;
    Pixel[][] colorImage;
    boolean[][] doneOnes;

    ArrayList < Line > lines;
    private Timer balltimer;

    private int counter;

    private int testAmt;

    //private int inkAmt;
    //private boolean[] brushPattern;

    String exportPath;
    /*
    Constructor to get the data from the two images using a image class
    */
    public AiProgram(String folderName) {
        String dir = System.getProperty("user.dir");
        String mainPath = dir.substring(0, dir.length() - 7);
        String newFolder = mainPath + "Output_Images/" + folderName + "Output";
        File folder = new File(mainPath + "Input_Images/" + folderName);
        new File(newFolder).mkdirs();
        exportPath = newFolder + "/";
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].getName().indexOf("jpg") != -1) {
                String fileName = listOfFiles[i].getPath();
                Image picture = new Image(fileName);
                colorImage = picture.getData();
            }
        }
        //currentAlive = new Pixel[currentImage.length][currentImage[0].length];
        currentImage = new Pixel[colorImage.length][colorImage[0].length];
        wallLines = new Pixel[colorImage.length][colorImage[0].length];
        doneOnes = new boolean[colorImage.length][colorImage[0].length];
        testAmt = 30;
        lines = new ArrayList < Line > ();
        edgeDetection();
        BallMover ballmover = new BallMover();
        balltimer = new Timer(3000, ballmover); //
        balltimer.start();
    }
    public void edgeDetection() {
        System.out.println("TEST: " + currentImage.length + ", " + currentImage[0].length);
        int[][] gausssssss = new int[][] {
            {
                0,
                0,
                1,
                2,
                1,
                0,
                0
            }, {
                0,
                3,
                13,
                22,
                13,
                3,
                0
            }, {
                1,
                13,
                59,
                97,
                59,
                13,
                1
            }, {
                2,
                22,
                97,
                159,
                97,
                22,
                2
            }, {
                1,
                13,
                59,
                97,
                59,
                13,
                1
            }, {
                0,
                3,
                13,
                22,
                13,
                3,
                0
            }, {
                0,
                0,
                1,
                2,
                1,
                0,
                0
            }
        };
        Pixel[][] grayscale = new Pixel[colorImage.length][colorImage[0].length];
        for (int row = 0; row < colorImage.length; row++) {
            for (int col = 0; col < colorImage[0].length; col++) {
                int avg = (colorImage[row][col].r + colorImage[row][col].g + colorImage[row][col].b) / 3;
                grayscale[row][col] = new Pixel(avg, avg, avg);
            }
        }
        //currentImage = new Pixel[colorImage.length][colorImage[0].length];
        Pixel[][] gauss = new Pixel[colorImage.length][colorImage[0].length];
        for (int row = 0; row < currentImage.length; row++) {
            for (int col = 0; col < currentImage[0].length; col++) {
                double tempBlur = 0;

                for (int rowI = row - 3; rowI <= row + 3; rowI++) {
                    for (int colI = col - 3; colI <= col + 3; colI++) {
                        if (inBounds(rowI, colI)) {
                            tempBlur += ((double) grayscale[rowI][colI].r) * gausssssss[rowI - row + 3][colI - col + 3];
                        }
                    }
                }
                tempBlur /= 1003.0;
                int temp = (int) Math.round(tempBlur);
                gauss[row][col] = new Pixel(temp, temp, temp);
                //currentImage[row][col] = grayscale[row][col];
            }
        }
        int[][] sobelX = new int[][] {
            {
                1,
                0,
                -1
            }, {
                2,
                0,
                -2
            }, {
                1,
                0,
                -1
            }
        };
        int[][] sobelY = new int[][] {
            {
                1,
                2,
                1
            }, {
                0,
                0,
                0
            }, {-1,
                -2,
                -1
            }
        };
        double[][] angles = new double[colorImage.length][colorImage[0].length];
        Pixel[][] sobel = new Pixel[colorImage.length][colorImage[0].length];
        for (int row = 0; row < currentImage.length; row++) {
            for (int col = 0; col < currentImage[0].length; col++) {
                double tempSobelX = 0;
                double tempSobelY = 0;
                for (int rowI = row - 1; rowI <= row + 1; rowI++) {
                    for (int colI = col - 1; colI <= col + 1; colI++) {
                        if (inBounds(rowI, colI)) {
                            tempSobelX += ((double) gauss[rowI][colI].r) * sobelX[rowI - row + 1][colI - col + 1];
                            tempSobelY += ((double) gauss[rowI][colI].r) * sobelY[rowI - row + 1][colI - col + 1];
                        }
                    }
                }
                int temp = (int) Math.round(Math.sqrt(tempSobelX * tempSobelX + tempSobelY * tempSobelY));
                //System.out.println(tempSobelX);
                //int temp = (int)Math.round(Math.abs(tempSobelX));
                sobel[row][col] = new Pixel(temp, temp, temp);
                angles[row][col] = Math.atan(tempSobelY / tempSobelX);
                //currentImage[row][col] = grayscale[row][col];
            }
        }
        Pixel[][] edge = new Pixel[colorImage.length][colorImage[0].length];
        for (int row = 0; row < currentImage.length; row++) {
            for (int col = 0; col < currentImage[0].length; col++) {
                int tempX = (int)(3 * Math.cos(angles[row][col]));
                int tempY = (int)(3 * Math.sin(angles[row][col]));
                if (inBounds(row + tempY, col + tempX) && inBounds(row - tempY, col - tempX)) {
                    if ((sobel[row][col].r > sobel[row + tempY][col + tempX].r && sobel[row][col].r > sobel[row - tempY][col - tempX].r))
                        edge[row][col] = sobel[row][col];
                    else
                        edge[row][col] = new Pixel();
                } else {
                    edge[row][col] = new Pixel();
                }
            }
        }
        int high = 200;
        int low = 30;
        for (int row = 0; row < currentImage.length; row++) {
            for (int col = 0; col < currentImage[0].length; col++) {
                if (edge[row][col].r < low) {
                    edge[row][col] = null;
                }
            }
        }

        int lineLen = currentImage[0].length / 150;
        Pixel[][] points = new Pixel[colorImage.length][colorImage[0].length];
        for (int row = 0; row < points.length; row++) {
            for (int col = 0; col < points[0].length; col++) {
                if (edge[row][col] != null) {
                    double xTemp = 0.0;
                    double yTemp = 0.0;
                    double angleTemp = angles[row][col] + (Math.PI / 2.0);
                    boolean breakOut = false;
                    for (int i = 0; i < lineLen && !breakOut; i++) {
                        xTemp += Math.cos(angleTemp);
                        yTemp += Math.sin(angleTemp);
                        if (inBounds(row + (int) yTemp, col + (int) xTemp)) {
                            if (edge[row + (int) yTemp][col + (int) xTemp] == null)
                                breakOut = true;
                        } else {
                            breakOut = true;
                        }
                    }
                    xTemp = 0.0;
                    yTemp = 0.0;
                    for (int i = 0; i < lineLen && !breakOut; i++) {
                        xTemp -= Math.cos(angleTemp);
                        yTemp -= Math.sin(angleTemp);
                        if (inBounds(row + (int) yTemp, col + (int) xTemp)) {
                            if (edge[row + (int) yTemp][col + (int) xTemp] == null)
                                breakOut = true;
                        } else {
                            breakOut = true;
                        }
                    }
                    if (!breakOut)
                        points[row][col] = new Pixel(255, 0, 0);
                }
            }
        }
        for (int row = 0; row < points.length; row++) {
            for (int col = 0; col < points[0].length; col++) {
                if (points[row][col] != null) {
                    double angleTemp = angles[row][col] + (Math.PI / 2.0);
                    for (double y = row - 2; y < row + 2; y++) {
                        double xTemp = 0.0;
                        double yTemp = 0.0;
                        while (inBounds((int)(y + yTemp), (int)(col + xTemp))) {
                            if (points[(int)(y + yTemp)][col + (int) xTemp] != null)
                                points[(int)(y + yTemp)][col + (int) xTemp] = null;
                            xTemp += 0.5 * Math.cos(angleTemp);
                            yTemp += 0.5 * Math.sin(angleTemp);
                        }
                        xTemp = 0.0;
                        yTemp = 0.0;
                        while (inBounds((int)(y + yTemp), (int)(col + xTemp))) {
                            if (points[(int)(y + yTemp)][col + (int) xTemp] != null)
                                points[(int)(y + yTemp)][col + (int) xTemp] = null;
                            xTemp -= 0.5 * Math.cos(angleTemp);
                            yTemp -= 0.5 * Math.sin(angleTemp);
                        }
                    }
                    points[(row)][col] = new Pixel(255, 0, 0);
                }
            }
        }
        //currentImage = new Pixel[colorImage.length][colorImage[0].length];
        for (int row = 0; row < points.length; row++) {
            for (int col = 0; col < points[0].length; col++) {
                if (points[row][col] != null) {
                    double angleTemp = angles[row][col] + (Math.PI / 2.0);
                    //double slope = Math.sin(angleTemp)/Math.cos(angleTemp);
                    double x1 = 0;
                    double y1 = 0;
                    double x2 = 0;
                    double y2 = 0;
                    double xTemp = 0.0;
                    double yTemp = 0.0;
                    while (inBounds((int)(row + yTemp + 0.5 * Math.sin(angleTemp)), (int)(col + xTemp + 0.5 * Math.cos(angleTemp)))) {
                        xTemp += 0.5 * Math.cos(angleTemp);
                        yTemp += 0.5 * Math.sin(angleTemp);
                    }
                    if (xTemp > 0) {
                        x1 = col + xTemp;
                        y1 = row + yTemp;
                    } else {
                        x2 = col + xTemp;
                        y2 = row + yTemp;
                    }
                    xTemp = 0.0;
                    yTemp = 0.0;
                    while (inBounds((int)(row + yTemp - 0.5 * Math.sin(angleTemp)), (int)(col + xTemp - 0.5 * Math.cos(angleTemp)))) {
                        xTemp -= 0.5 * Math.cos(angleTemp);
                        yTemp -= 0.5 * Math.sin(angleTemp);
                    }
                    if (xTemp > 0) {
                        x1 = col + xTemp;
                        y1 = row + yTemp;
                    } else {
                        x2 = col + xTemp;
                        y2 = row + yTemp;
                    }
                    lines.add(new Line((double)(col), (double)(currentImage.length - row), x1, currentImage.length - y1, x2, currentImage.length - y2));
                }
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            for (int z = 0; z < lines.size(); z++) {
                if (i != z && lines.get(i).literallyTheSameLine(lines.get(z))) {
                    lines.remove(z);
                    z--;
                }
            }
        }
        for (int i = 0; i < lines.size(); i++) {

            //lines.get(i).printInfo();
        }
        for (int i = 0; i < lines.size(); i++) {
            for (int z = 0; z < lines.size(); z++) {
                if (i != z)
                    lines.get(i).findInter(lines.get(z));
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            double x2 = lines.get(i).x2;
            double y2 = lines.get(i).y2;
            //System.out.println((int)lines.get(i).x1 + ", " + (int)lines.get(i).x1 + ", " + (int)lines.get(i).x2 + ", " + (int)lines.get(i).y2);
            while (x2 < lines.get(i).x1) {
                //System.out.println((int)x2 + ", " + (int)y2);
                if (inBounds(currentImage.length - (int) y2, (int) x2))
                    currentImage[currentImage.length - (int) y2][(int) x2] = new Pixel(0, 0, 0); //currentImage.length-
                x2 += 0.0003;
                y2 += 0.0003 * lines.get(i).slope;
            }
        }
        int multCounter = 0;
        System.out.println("MaxLen " + Math.sqrt(Math.pow(currentImage.length, 2) + Math.pow(currentImage[0].length, 2)));
        while (!allDone()) {
            multCounter++;
            for (int i = 0; i < lines.size(); i++) {
                Line temp = lines.get(i);
                if (!temp.finished1) {
                    double tempx1 = temp.x1 + 0.03 * multCounter; //0.03
                    double tempy1 = temp.y1 + 0.03 * temp.slope * multCounter;
                    //System.out.println(i + ": " + tempx1 + ", " + tempy1 + "\t" + (int)tempx1 != (int)temp.x1 && (int)tempy1 != (int)temp.y1);
                    if ((int) tempx1 != (int) temp.x1 || (int) tempy1 != (int) temp.y1) {
                        if (!inBounds(currentImage.length - (int) tempy1, (int) tempx1)) {
                            temp.finished1 = true;
                            temp.x1 = tempx1;
                            temp.y1 = tempy1;
                        } else if (currentImage[currentImage.length - (int) tempy1][(int) tempx1] != null) {
                            temp.finished1 = true;
                            temp.x1 = tempx1;
                            temp.y1 = tempy1;
                        }
                    }
                }
                if (!temp.finished2) {
                    double tempx2 = temp.x2 - 0.03 * multCounter;
                    double tempy2 = temp.y2 - 0.03 * temp.slope * multCounter;
                    //System.out.println(i + ": " + tempx2 + ", " + tempy2);
                    if ((int) tempx2 != (int) temp.x2 || (int) tempy2 != (int) temp.y2) {
                        if (!inBounds(currentImage.length - (int) tempy2, (int) tempx2)) {
                            temp.finished2 = true;
                            temp.x2 = tempx2;
                            temp.y2 = tempy2;
                        } else if (currentImage[currentImage.length - (int) tempy2][(int) tempx2] != null) {
                            temp.finished2 = true;
                            temp.x2 = tempx2;
                            temp.y2 = tempy2;
                        }
                    }
                }
            }
        }
        System.out.println(multCounter);
        currentImage = new Pixel[colorImage.length][colorImage[0].length];
        for (int i = 0; i < lines.size(); i++) {
            double x2 = lines.get(i).x2;
            double y2 = lines.get(i).y2;
            //System.out.println((int)lines.get(i).x1 + ", " + (int)lines.get(i).x1 + ", " + (int)lines.get(i).x2 + ", " + (int)lines.get(i).y2);
            while (x2 < lines.get(i).x1) {
                //System.out.println((int)x2 + ", " + (int)y2);
                if (inBounds(currentImage.length - (int) y2, (int) x2))
                    currentImage[currentImage.length - (int) y2][(int) x2] = new Pixel(0, 0, 0); //currentImage.length-
                x2 += 0.0003;
                y2 += 0.0003 * lines.get(i).slope;
            }
        }
    }
    private boolean allDone() {
        //for(int i = 0; i < lines.size(); i++)
        //System.out.println(lines.get(i).finished1 + ", " + lines.get(i).finished2);
        for (int i = 0; i < lines.size(); i++)
            if (!lines.get(i).finished1 || !lines.get(i).finished2)
                return false;
        return true;
    }
    public boolean inBounds(int row, int col) {
        return row >= 0 && col >= 0 && row < currentImage.length && col < currentImage[0].length;
    }
    public int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    /*
    The "update function"
    */
    class BallMover implements ActionListener {
        public void actionPerformed(ActionEvent e) {
          if(count() <= 300)
          {
            for(int row = 0; row < currentImage.length; row++)
              for(int col = 0; col < currentImage[0].length; col++)
                if(currentImage[row][col] == null)
                  currentImage[row][col] = new Pixel();
            Image newPicture = new Image(currentImage);
            newPicture.exportImage(exportPath+"OutputImg");
            System.exit(0);
          }
          for(int repeat = 0; repeat < 100; repeat++)
          {
            counter++;
            for (int randTest = 0; randTest < (currentImage.length * currentImage[0].length) / 1.5; randTest++) {
                int row = randomWithRange(0, currentImage.length - 1);
                int col = randomWithRange(0, currentImage[0].length - 1);
                //System.out.println(doneOnes[row][col]);
                if (currentImage[row][col] != null) {
                    int numBabies = ((80 - currentImage[row][col].comparePixel(colorImage[row][col])) / 10) + (counter / 100);
                    if (numBabies < 0)
                        numBabies = 0;
                    int babyCounter = 0;
                    for (int i = 1; i <= numBabies; i++) {
                        int babyRange = 1;
                        int checkCol = randomWithRange(-1 * babyRange + col, babyRange + col);//currentAlive[checkRow][checkCol] == null
                        int checkRow = randomWithRange(-1 * babyRange + row, babyRange + row);//&& checkCol != col && checkRow != row &&
                        if (inBounds(checkRow, checkCol) && currentImage[checkRow][checkCol] == null) {
                            babyCounter++;
                            currentImage[checkRow][checkCol] = geiWoBaby(currentImage[row][col], babyCounter);
                        }
                    }
                }
            }
            //wallLines
            //System.out.println(counter);
            System.out.println("Number Of Empty Pixels "+(1.0-((count())/((double)((currentImage.length * currentImage[0].length)))))) + "%";
            //repaint();
          }
          /*
          for (int row = 0; row < doneOnes.length; row++) {
              for (int col = 0; col < doneOnes[0].length; col++) {
                if(!doneOnes[row][col] && currentImage[row][col] != null)
                {
                  boolean temp = false;
                  for(int rowT = row - 1; rowT <= row + 1 && !temp; rowT++)
                  {
                    for(int colT = col - 1; colT <= col + 1 && !temp; colT++)
                    {
                      if(inBounds(rowT, colT) && currentImage[rowT][colT] == null)
                      {
                        temp = true;
                      }
                    }
                  }
                  if(temp)
                  {
                    doneOnes[row][col] = true;
                  }
                }
              }
            }*/
          repaint();
        }
    }

    public Pixel geiWoBaby(Pixel parent, int mutationAmount) {
        mutationAmount = (int) Math.pow(2, mutationAmount);
        return new Pixel(parent.r + randomWithRange(-1 * mutationAmount, mutationAmount), parent.g + randomWithRange(-1 * mutationAmount, mutationAmount), parent.b + randomWithRange(-1 * mutationAmount, mutationAmount));
    }
    /*
    This method draws the pixels on screen
    */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < currentImage.length; row++)
            for (int col = 0; col < currentImage[0].length; col++) {
                if (currentImage[row][col] != null) {
                    g.setColor(currentImage[row][col].getColor());
                } else {
                    g.setColor(Color.BLACK);
                }
                g.drawLine(col, row, col, row);
            }
    }
    public int count() {
        int counter = 0;
        for (int row = 0; row < currentImage.length; row++)
            for (int col = 0; col < currentImage[0].length; col++)
                if (currentImage[row][col] == null)
                    counter++;
        return counter;
    }

    public Pixel[][] cloneArray(Pixel[][] cloneThis) {
        Pixel[][] newArray = new Pixel[cloneThis.length][cloneThis[0].length];
        for (int row = 0; row < newArray.length; row++)
            for (int col = 0; col < newArray[0].length; col++)
                newArray[row][col] = cloneThis[row][col];
        return newArray;
    }
}
