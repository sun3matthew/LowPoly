import java.awt.Color;

public class Pixel
{
  int r;
  int g;
  int b;
  public Pixel()
  {
    r = 0;
    g = 0;
    b = 0;
  }
  public Pixel(int rIn, int gIn, int bIn)
  {
    this();
    setColor(rIn, gIn, bIn);
  }
  public Pixel(Pixel in)
  {
    this(in.r, in.g, in.b);
  }
  public Pixel(Color colorIn)
  {
    this(colorIn.getRed(), colorIn.getGreen(), colorIn.getBlue());
  }

  public void setColor(int rIn, int gIn, int bIn)
  {
    if(rIn >= 255)
    {
      r = 255;
    }else if(rIn < 0)
    {
      r = 0;
    }else
    {
      r = rIn;
    }
    if(gIn >= 255)
    {
      g = 255;
    }else if(gIn < 0)
    {
      g = 0;
    }else
    {
      g = gIn;
    }
    if(bIn >= 255)
    {
      b = 255;
    }else if(bIn < 0)
    {
      b = 0;
    }else
    {
      b = bIn;
    }
  }
  public Color getColor()
  {
    return new Color(r, g, b);
  }
  public int comparePixel(Pixel compareThis)
  {
    return Math.abs(compareThis.r-this.r)+Math.abs(compareThis.g-this.g)+Math.abs(compareThis.b-this.b);
    //return Math.abs((compareThis.r-this.r)+(compareThis.g-this.g)+(compareThis.b-this.b));
  }
}
