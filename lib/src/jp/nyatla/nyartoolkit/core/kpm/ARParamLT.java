package jp.nyatla.nyartoolkit.core.kpm;

import jp.nyatla.nyartoolkit.core.param.NyARParam;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.param.distfactor.INyARCameraDistortionFactor;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

/**
 * @todo
 * このクラスは歪み配列マップの動的生成だけだから後でリファクタリングして消す。
 * @author nyatla
 *
 */

/*!
@typedef 
@abstract   Structure holding camera parameters, in lookup table form.
@discussion
    ARToolKit's tracking depends on accurate knowledge of the properties of the
    imaging system used to acquire input images. This structure holds the properties
    of an imaging system for internal use in ARToolKit. This information is used
    throughout the entire ARToolKit pipeline, including knowing the size of images
    being returned by the video library, marker detection and pose estimation,
    and warping of camera images for video-see-through registration.

    This version of the structure contains a pre-calculated lookup table of
    values covering the camera image width and height, plus a padded border.
@field      param A copy of original ARParam from which the lookup table was calculated.
@field      paramLTf The lookup table.
*/
public class ARParamLT extends NyARParam{
    public float[] i2o;
    public float[] o2i;
    public int      xsize;
    public int      ysize;
    public int      xOff;
    public int      yOff;
    final static int AR_PARAM_LT_DEFAULT_OFFSET =15;
    /**
    Allocate and calculate a lookup-table camera parameter from a standard camera parameter.
    A lookup-table based camera parameter offers significant performance
    savings in certain ARToolKit operations (including unwarping of pattern spaces)
    compared to use of the standard camera parameter.

    The original ARParam camera parameters structure is copied into the ARParamLT
    structure, and is available as paramLT->param.
    @param param A pointer to an ARParam structure from which the lookup table will be generaeted.
        This ARParam structure will be copied, and the original may be disposed of.
    @param i_offeset
    	An integer value which specifies how much the lookup table values will be
        padded around the original camera parameters size. Normally, the default value
        AR_PARAM_LT_DEFAULT_OFFSET (defined elsewhere in this header) should be used. However,
        when using a camera with a large amount of lens distortion, a higher value may be
        required to cope with the corners or sides of the camera image frame.
    @result A pointer to a newly-allocated ARParamLT structure, or NULL if an error
        occurred. Once the ARParamLT is no longer needed, it should be disposed
        of by calling arParamLTFree() on it.
    @seealso arParamLTFree arParamLTFree
 */    
    public ARParamLT(NyARParam i_param)
    {
    	this(i_param.getScreenSize(),i_param.getPerspectiveProjectionMatrix(),i_param.getDistortionFactor(),AR_PARAM_LT_DEFAULT_OFFSET);
    }
	public ARParamLT(NyARIntSize i_screen_size,
			NyARPerspectiveProjectionMatrix i_projection_mat,
			INyARCameraDistortionFactor i_dist_factor,int i_offset) {
		super(i_screen_size, i_projection_mat, i_dist_factor);

	    
	    this.xsize = this._screen_size.w + i_offset*2;
	    this.ysize = this._screen_size.h + i_offset*2;
	    this.xOff = i_offset;
	    this.yOff = i_offset;
	    this.i2o=new float[this.xsize*this.ysize*2];
	    this.o2i=new float[this.xsize*this.ysize*2];
	    NyARDoublePoint2d tmp=new NyARDoublePoint2d();//
	    for(int j = 0; j < this.ysize; j++ ) {
	        for(int i = 0; i < this.xsize; i++ ) {
	        	int ptr=(j*this.xsize+i)*2;
	        	i_dist_factor.ideal2Observ(i-i_offset,j-i_offset, tmp);
	        	this.i2o[ptr+0]=(float)tmp.x;
	        	this.i2o[ptr+1]=(float)tmp.y;
	        	i_dist_factor.observ2Ideal(i-i_offset,j-i_offset, tmp);
	        	this.o2i[ptr+0]=(float)tmp.x;
	        	this.o2i[ptr+1]=(float)tmp.y;	        	
	        }
	    }
	}
	public int arParamIdeal2ObservLTf(float  ix, float  iy,NyARDoublePoint2d o)
	{
	    int      px, py;
	    
	    px = (int)(ix+0.5F) + this.xOff;
	    py = (int)(iy+0.5F) + this.yOff;
	    if( px < 0 || px >= this.xsize || py < 0 || py >= this.ysize ){
	    	return -1;
	    }
	    
	    int lt =  (py*this.xsize + px)*2;
	    o.x = this.i2o[lt+0];
	    o.y = this.i2o[lt+1];
	    return 0;
	}
	public int arParamObserv2IdealLTf(float  ox, float  oy,NyARDoublePoint2d o)
	{
	    int      px, py;
	    
	    px = (int)(ox+0.5F) + this.xOff;
	    py = (int)(oy+0.5F) + this.yOff;
	    if( px < 0 || px >= this.xsize || py < 0 || py >= this.ysize ){
	    	return -1;
	    }
	    
	    int lt = (py*this.xsize + px)*2;
	    o.x = o2i[lt+0];
	    o.y = o2i[lt+1];
	    return 0;
	}	
}
