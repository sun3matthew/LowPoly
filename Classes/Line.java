import java.util.ArrayList;

public class Line
{
  public double slope;
  public double yInt;

  public double x1, y1, x2, y2;

  public double centerX, centerY;

  public double interX, interY;

  public boolean finished1, finished2;
  public Line(double centerXIn, double centerYIn, double x1In, double y1In, double x2In, double y2In)
  {
    centerX = centerXIn;
    centerY = centerYIn;
    x1 = x1In;
    y1 = y1In;
    x2 = x2In;
    y2 = y2In;
    slope = (y2-y1)/(x2-x1);
    yInt = (-1*slope*x1)+y1;
  }
  public void findInter(Line lineIn)
  {
    interX = (lineIn.yInt-yInt)/(slope-lineIn.slope);
    interY = slope*interX+yInt;
    if(interX >= centerX)
    {
      if(interX < x1)
      {
        x1 = interX;
        y1 = interY;
      }
    }else
    {
      if(interX > x2)
      {
        x2 = interX;
        y2 = interY;
      }
    }
  }
  public void printInfo()
  {
    System.out.println("Slope "+slope);
    System.out.println("yInt "+yInt);
    System.out.println("Center "+centerX + ", " + centerY);
    System.out.println(x1 + ", " + y1+ ", " + x2+ ", " + y2);
  }
  public boolean literallyTheSameLine(Line line2)
  {
    return (Math.abs(yInt-line2.yInt) < 20)&&(Math.abs(slope-line2.slope) < 0.9);
  }
  public double getDist(double x1In, double y1In, double x2In, double y2In)
  {
    return Math.sqrt(Math.pow((x2In-x1In),2)+Math.pow((y2In-y1In),2));
  }
}
