package jp.nyatla.nyartoolkit.core.kpm.dogscalepyramid;

import jp.nyatla.nyartoolkit.core.NyARRuntimeException;
import jp.nyatla.nyartoolkit.core.kpm.KpmImage;
import jp.nyatla.nyartoolkit.core.raster.INyARRaster;
import jp.nyatla.nyartoolkit.core.types.NyARBufferType;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class LaplacianImage implements INyARRaster {
	private NyARIntSize _size;
	private double[] _buf;

	public LaplacianImage(int i_width, int i_height) {
		this._size = new NyARIntSize(i_width, i_height);
		this._buf = new double[i_width * i_height];
	}

	@Override
	public int getWidth() {
		return this._size.w;
	}

	@Override
	public int getHeight() {
		return this._size.h;
	}

	@Override
	public NyARIntSize getSize() {
		return this._size;
	}

	@Override
	public Object getBuffer() {
		return this._buf;
	}

	@Override
	public int getBufferType() {
		return NyARBufferType.USER_DEFINE;
	}

	@Override
	public boolean isEqualBufferType(int i_type_value) {
		return false;
	}

	@Override
	public boolean hasBuffer() {
		return true;
	}

	@Override
	public void wrapBuffer(Object i_ref_buf) {
		NyARRuntimeException.notImplement();
	}

	@Override
	public Object createInterface(Class<?> i_iid) {
		throw new NyARRuntimeException();
	}

	// これどうにかしよう
	public int get(int i_row) {
		return this._size.w * i_row;
	}


    /**
     * Perform bilinear interpolation.
     * Port from bilinear_interpolation function.
     * @param[in] x x-location to interpolate
     * @param[in] y y-location to interpolate
     */
	public double bilinearInterpolation(double x, double y)
	{
		double[] buf=this._buf;
		int width=this._size.w;
		double w0, w1, w2, w3;
		// Compute location of 4 neighbor pixels
		int xp = (int) x;
		int yp = (int) y;
		int xp_plus_1 = xp + 1;
		int yp_plus_1 = yp + 1;


		// Pointer to 2 image rows
		int p0 = width * yp;// p0 = (const Tin*)((const unsigned char*)im+step*yp);
		int p1 = p0 + width;// p1 = (const Tin*)((const unsigned char*)p0+step);

		// Compute weights
		w0 = (xp_plus_1 - x) * (yp_plus_1 - y);
		w1 = (x - xp) * (yp_plus_1 - y);
		w2 = (xp_plus_1 - x) * (y - yp);
		w3 = (x - xp) * (y - yp);

		// Compute weighted pixel
		return w0 * buf[p0 + xp] + w1 * buf[p0 + xp_plus_1] + w2 * buf[p1 + xp] + w3 * buf[p1 + xp_plus_1];
	}

	
	
	/**
	 * Compute the difference image.
	 * 
	 * d = im1 - im2
	 */
	public void difference_image_binomial(KpmImage im1, KpmImage im2) {
		// Compute diff
		double[] p0 = (double[]) this.getBuffer();
		double[] p1 = (double[]) im1.getBuffer();
		double[] p2 = (double[]) im2.getBuffer();
		for (int i = im1.getWidth()*im1.getHeight()-1; i>=0 ; i--) {
			p0[i] = p1[i] - p2[i];
		}
		return;
	}
	// private void ComputeSubpixelDerivatives(
	// float& Dx, float& Dy,
	// float& Dxx,float& Dyy,float& Dxy,
	// const Image& im,
	// int x,int y)
	public void computeSubpixelDerivatives(int x, int y,double[] dn)
	{

		double[] im_buf = (double[]) this.getBuffer();
		int pm1 = this.get(y - 1) + x;
		int p = this.get(y) + x;
		int pp1 = this.get(y + 1) + x;

		// Dx = 0.5f*(p[1]-p[-1]);
		// Dy = 0.5f*(pp1[0]-pm1[0]);
		// Dxx = p[-1] + (-2.f*p[0]) + p[1];
		// Dyy = pm1[0] + (-2.f*p[0]) + pp1[0];
		// Dxy = 0.25f*((pm1[-1] + pp1[1]) - (pm1[1] + pp1[-1]));
		dn[0] = 0.5f * (im_buf[p + 1] - im_buf[p - 1]);
		dn[1] = 0.5f * (im_buf[pp1 + 0] - im_buf[pm1 + 0]);
		dn[2] = im_buf[p - 1] + (-2.f * im_buf[p + 0]) + im_buf[p + 1];
		dn[3] = im_buf[pm1 + 0] + (-2.f * im_buf[p + 0]) + im_buf[pp1 + 0];
		dn[4] = 0.25f * ((im_buf[pm1 - 1] + im_buf[pp1 + 1]) - (im_buf[pm1 + 1] + im_buf[pp1 - 1]));
		return;
	}	
}