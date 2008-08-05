/**
 * NyARRaster_RGBにOpenGL向け関数を追加したもの
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.java3d.utils;

import java.awt.image.*;
import java.awt.color.*;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;



import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.NyARParam;
import jp.nyatla.nyartoolkit.jmf.utils.*;

/**
 * 
 * このクラスは、Java3Dと互換性のあるNyARToolkitのラスタイメージを保持します。
 *
 */
public class J3dNyARRaster_RGB extends JmfNyARRaster_RGB
{
    private ImageComponent2D imc2d;
    private byte[] i2d_buf;
    private BufferedImage bufferd_image;

    /**
     * JMFのキャプチャ画像をこのクラスのBufferedImageにコピーします。
     * @param i_buffer
     * 画像の格納されたバッファを指定して下さい。
     * 画像サイズはコンストラクタで与えたパラメタと同じサイズである必要があります。
     */
    public void setBuffer(javax.media.Buffer i_buffer) throws NyARException
    {
        super.setBuffer(i_buffer);
        //メモ：この時点では、ref_dataにはi_bufferの参照値が入ってる。
        synchronized(this){
            //キャプチャデータをi2dのバッファにコピーする。（これ省略したいなあ…。）
            System.arraycopy(this.ref_buf,0,this.i2d_buf,0,this.i2d_buf.length);
        }
	//ここでref_bufの参照値をref_bufへ移動
        this.ref_buf=this.i2d_buf;
    }  
    public J3dNyARRaster_RGB(NyARParam i_cparam)
    {
	super(i_cparam.getX(),i_cparam.getY());

	//RGBのラスタを作る。
	this.bufferd_image = new BufferedImage(this.width,this.height,BufferedImage.TYPE_3BYTE_BGR);
	i2d_buf=((DataBufferByte)bufferd_image.getRaster().getDataBuffer()).getData();
        this.imc2d= new ImageComponent2D(ImageComponent2D.FORMAT_RGB, this.bufferd_image, true, true);
        imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
    }
    /**
     * 自身の格納しているImageComponent2Dオブジェクトを作り直します。
     * Java3D1.5がDirectXで動いた（らしいとき）に、ImageComponent2Dのインスタンス
     * IDが異ならないと、Behavior内でイメージの更新を通知できない事象に対応するために実装してあります。
     * Behavior内でgetImageComponent2()関数を実行する直前に呼び出すことで、この事象を回避することができます。
     * 
     */
    public void renewImageComponent2D()
    {
        this.imc2d= new ImageComponent2D(ImageComponent2D.FORMAT_RGB, this.bufferd_image, true, true);
        this.imc2d.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);	
    }
    public ImageComponent2D getImageComponent2D()
    {
	return this.imc2d;
    }
}