/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2012 Ryo Iizuka
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as publishe
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.param.distfactor;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.types.*;

/**
 * LookupTableを使った樽型歪み設定/解除クラスです。
 * ARToolKitV5のParamsLTのアルゴリズムを使います。
 * {@link #changeScale(double, double)}は使用できません。
 */
public class NyARCameraDistortionFactorLT extends NyARCameraDistortionFactorImpl
{	
    private double[] _i2o;
    private double[] _o2i;
    private int      _xsize;
    private int      _ysize;
    private int      _xOff;
    private int      _yOff;
		
	public NyARCameraDistortionFactorLT(NyARIntSize i_screen_size,int i_offset,INyARCameraDistortionFactor i_base_factor)
	{
	    this._xsize = i_screen_size.w + i_offset*2;
	    this._ysize = i_screen_size.h + i_offset*2;
	    this._xOff = i_offset;
	    this._yOff = i_offset;
	    this._i2o=new double[this._xsize*this._ysize*2];
	    this._o2i=new double[this._xsize*this._ysize*2];
	    NyARDoublePoint2d tmp=new NyARDoublePoint2d();//
	    for(int j = 0; j < this._ysize; j++ ) {
	        for(int i = 0; i < this._xsize; i++ ) {
	        	int ptr=(j*this._xsize+i)*2;
	        	i_base_factor.ideal2Observ(i-i_offset,j-i_offset, tmp);
	        	this._i2o[ptr+0]=(float)tmp.x;
	        	this._i2o[ptr+1]=(float)tmp.y;
	        	i_base_factor.observ2Ideal(i-i_offset,j-i_offset, tmp);
	        	this._o2i[ptr+0]=(float)tmp.x;
	        	this._o2i[ptr+1]=(float)tmp.y;	        	
	        }
	    }
		return;
		
	}
	/**
	 * この関数は、歪みパラメータをスケール倍します。
	 * パラメータ値は、スケール値の大きさだけ、拡大、又は縮小します。
	 * @param i_x_scale
	 * x方向のパラメータ倍率
	 * @param i_y_scale
	 * y方向のパラメータ倍率
	 */
	@Override
	public void changeScale(double i_x_scale,double i_y_scale)
	{
		throw new NyARRuntimeException();
	}
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_in
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(double i_x,double i_y, NyARDoublePoint2d o_out)
	{
	    int px = (int)(i_x+0.5) + this._xOff;
	    int py = (int)(i_y+0.5) + this._yOff;
	    if( px < 0 || px >= this._xsize || py < 0 || py >= this._ysize ){
			throw new NyARRuntimeException();
	    }
	    
	    int lt =  (py*this._xsize + px)*2;
	    o_out.x = this._i2o[lt+0];
	    o_out.y = this._i2o[lt+1];
	}
	
	/**
	 * この関数は、座標点を理想座標系から観察座標系へ変換します。
	 * @param i_x
	 * 変換元の座標
	 * @param i_y
	 * 変換元の座標
	 * @param o_out
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void ideal2Observ(double i_x,double i_y, NyARIntPoint2d o_out)
	{
	    int px = (int)(i_x+0.5) + this._xOff;
	    int py = (int)(i_y+0.5) + this._yOff;
	    if( px < 0 || px >= this._xsize || py < 0 || py >= this._ysize ){
			throw new NyARRuntimeException();
	    }
	    
	    int lt =  (py*this._xsize + px)*2;
	    o_out.x = (int)this._i2o[lt+0];
	    o_out.y = (int)this._i2o[lt+1];
	}
	
	/**
	 * この関数は、座標を観察座標系から理想座標系へ変換します。
	 * @param ix
	 * 変換元の座標
	 * @param iy
	 * 変換元の座標
	 * @param o_point
	 * 変換後の座標を受け取るオブジェクト
	 */
	public void observ2Ideal(double ix, double iy, NyARDoublePoint2d o_point)
	{
	    int px = (int)(ix+0.5) + this._xOff;
	    int py = (int)(iy+0.5) + this._yOff;
	    if( px < 0 || px >= this._xsize || py < 0 || py >= this._ysize ){
			throw new NyARRuntimeException();
	    }
	    
	    int lt = (py*this._xsize + px)*2;
	    o_point.x = this._o2i[lt+0];
	    o_point.y = this._o2i[lt+1];
	}
	@Override
	public void observ2Ideal(int ix, int iy, NyARDoublePoint2d o_point)
	{
	    int px = (int)(ix+0.5) + this._xOff;
	    int py = (int)(iy+0.5) + this._yOff;
	    if( px < 0 || px >= this._xsize || py < 0 || py >= this._ysize ){
			throw new NyARRuntimeException();
	    }
	    
	    int lt = (py*this._xsize + px)*2;
	    o_point.x = this._o2i[lt+0];
	    o_point.y = this._o2i[lt+1];
	}


}
