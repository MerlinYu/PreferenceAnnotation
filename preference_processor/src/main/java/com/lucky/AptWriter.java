package com.lucky;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by yuchao on 9/6/16.
 */
public class AptWriter extends JavaWriter{

  public JavaWriter javaWriter;
  public Writer writer;

  /**
   * @param out the stream to which Java source will be written. This should be a buffered stream.
   */
  public AptWriter(Writer out) {
    super(out);
    writer = out;
  }



  public void writeCode(String format) throws IOException{
    if (writer == null) {
      throw new NullPointerException("writer is null");
    }
    writer.write(format);


  }


}
