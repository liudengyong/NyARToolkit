package jp.nyatla.nyartoolkit.core.kpm;

import java.util.List;
import java.util.ArrayList;

import jp.nyatla.nyartoolkit.core.ar2.base.ARParamLT;
import jp.nyatla.nyartoolkit.core.kpm.base.FeatureVector;
import jp.nyatla.nyartoolkit.core.kpm.base.FreakFeature;
import jp.nyatla.nyartoolkit.core.kpm.base.KpmCoord2D;
import jp.nyatla.nyartoolkit.core.kpm.base.KpmInputDataSet;
import jp.nyatla.nyartoolkit.core.kpm.base.KpmRefData;
import jp.nyatla.nyartoolkit.core.kpm.base.KpmResult;
import jp.nyatla.nyartoolkit.core.kpm.base.Point3d;
import jp.nyatla.nyartoolkit.core.kpm.base.Point3dVector;
import jp.nyatla.nyartoolkit.core.kpm.base.kpmMatching;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.facade.VisualDatabaseFacade;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.FeaturePoint;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.FeaturePointStack;
import jp.nyatla.nyartoolkit.core.kpm.base.vision.matchers.matchStack;
import jp.nyatla.nyartoolkit.core.marker.nft.NyARNftFreakFsetFile;
import jp.nyatla.nyartoolkit.core.raster.gs.INyARGrayscaleRaster;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARIntSize;

public class FreakKeypointMatching {
	private final static int FREAK_SUB_DIMENSION = 96;
	VisualDatabaseFacade freakMatcher;
	final ARParamLT cparamLT;
	int poseMode;
	int xsize;
	int ysize;
	int procMode;
	int detectedMaxFeature;
	KpmRefData[] refDataSet;
	KpmInputDataSet inDataSet=new KpmInputDataSet();
	KpmResult[] result;
	int resultNum;
	final public static int KpmPose6DOF = 1;
	final public static int KpmPoseHomography = 2;

	final public static int KpmProcFullSize = 1;
	final public static int KpmProcHalfSize = 2;
	final public static int KpmProcQuatSize = 3;
	final public static int KpmProcOneThirdSize = 4;
	final public static int KpmProcTwoThirdSize = 5;
	final public static int KpmDefaultProcMode = KpmProcFullSize;

	public FreakKeypointMatching(ARParamLT cparamLT) {
		this(cparamLT, cparamLT.getScreenSize(), KpmPose6DOF);
	}

	public FreakKeypointMatching(ARParamLT cparamLT, NyARIntSize size, int poseMode) {
		this.freakMatcher = new VisualDatabaseFacade();

		this.cparamLT = cparamLT;
		this.poseMode = poseMode;
		this.xsize = size.w;
		this.ysize = size.h;
		this.procMode = KpmDefaultProcMode;
		this.detectedMaxFeature = -1;

		this.refDataSet=null;

		this.inDataSet.coord = null;
		this.inDataSet.num = 0;

		this.result = new KpmResult[1];
		this.result[0]=new KpmResult();
		this.resultNum = 0;

	}

	public int kpmMatching(INyARGrayscaleRaster inImage) {

		int procMode;
		FeatureVector featureVector = new FeatureVector();
		int i, j;
		int ret;

		procMode = this.procMode;

		this.freakMatcher.query(inImage);
		this.inDataSet.num = featureVector.num = (int) this.freakMatcher
				.getQueryFeaturePoints().getLength();


		if (this.inDataSet.num != 0) {

			this.inDataSet.coord = KpmCoord2D.creaeArray(this.inDataSet.num);

			featureVector.sf = FreakFeature.createArray(this.inDataSet.num);


			FeaturePointStack points = this.freakMatcher
					.getQueryFeaturePoints();
			byte[] descriptors = this.freakMatcher.getQueryDescriptors();
			if (procMode == KpmProcFullSize) {
				for (i = 0; i < this.inDataSet.num; i++) {

					double x = points.getItem(i).x, y = points.getItem(i).y;
					for (j = 0; j < FREAK_SUB_DIMENSION; j++) {
						featureVector.sf[i].v[j] = descriptors[i
								* FREAK_SUB_DIMENSION + j];
					}
					if (this.cparamLT != null) {
						NyARDoublePoint2d tmp = new NyARDoublePoint2d();
						this.cparamLT.arParamObserv2IdealLTf(x, y, tmp);
						this.inDataSet.coord[i].x =  tmp.x;
						this.inDataSet.coord[i].y =  tmp.y;
					} else {
						this.inDataSet.coord[i].x = x;
						this.inDataSet.coord[i].y = y;
					}
				}
			}

			for (int pageLoop = 0; pageLoop < this.resultNum; pageLoop++) {

				this.result[pageLoop].camPoseF = -1;
				matchStack matches = this.freakMatcher.inliers();
				int matched_image_id = this.freakMatcher.matchedId();
				if (matched_image_id < 0)
					continue;

				ret = kpmMatching.kpmUtilGetPose_binary(this.cparamLT, matches,
						this.freakMatcher.get3DFeaturePoints(matched_image_id),
						this.freakMatcher.getQueryFeaturePoints(),
						this.result[pageLoop]);
				if (ret == 0) {
					this.result[pageLoop].camPoseF = 0;
					this.result[pageLoop].inlierNum = (int) matches.getLength();
				}
			}
		} else {
			for (i = 0; i < this.resultNum; i++) {
				this.result[i].camPoseF = -1;
			}
		}

		for (i = 0; i < this.resultNum; i++) {
			this.result[i].skipF = false;
		}

		return 0;
	}
	public int kpmSetRefDataSet(NyARNftFreakFsetFile i_refDataSet)
	{

    	this.resultNum=1;

        
        int db_id = 0;
        for (int k = 0; k < i_refDataSet.page_info.length; k++) {
            for (int m = 0; m < i_refDataSet.page_info[k].image_info.length; m++)
            {
            	int image_no=i_refDataSet.page_info[k].image_info[m].image_no;
        		List<Point3d> p3dl=new ArrayList<Point3d>();
        		List<FeaturePoint> fpl=new ArrayList<FeaturePoint>();
        		int desc_len=0;
            	for(int i=0;i<i_refDataSet.ref_point.length;i++){
            		if(i_refDataSet.ref_point[i].refImageNo==image_no){
            			NyARNftFreakFsetFile.RefDataSet t=i_refDataSet.ref_point[i];
            			FeaturePoint fp=new FeaturePoint(); 
            			fp.x=t.coord2D.x;
            			fp.y=t.coord2D.y;
            			fp.angle=t.featureVec.angle;
            			fp.scale=t.featureVec.scale;
            			fp.maxima=t.featureVec.maxima>0?true:false;
            			fpl.add(fp);
            			//descripterはサイズだけ先に計算
            			desc_len+=i_refDataSet.ref_point[i].featureVec.v.length;
            			//
            			Point3d point3d=new Point3d();
            			point3d.x=t.coord3D.x;
            			point3d.y=t.coord3D.y;
            			p3dl.add(point3d);
            			
            		}
            	}
            	//descripterの生成
        		byte[] dl=new byte[desc_len];
            	int l=0;
            	for(int i=0;i<i_refDataSet.ref_point.length;i++){
            		if(i_refDataSet.ref_point[i].refImageNo==image_no){
            			for(int j=0;j<i_refDataSet.ref_point[i].featureVec.v.length;j++){
	            			dl[l]=i_refDataSet.ref_point[i].featureVec.v[j];
	            			l++;
            			}
            		}
            	}
            	FeaturePointStack fps=new FeaturePointStack(fpl.size());
            	for(FeaturePoint i:fpl){
            		FeaturePoint p=fps.prePush();
            		p.set(i);
            	}
            	Point3dVector p3v=new Point3dVector(p3dl.size());
            	for(Point3d i:p3dl){
            		Point3d p=p3v.prePush();
            		p.x=i.x;
            		p.y=i.y;
            		p.z=i.z;
            	}
            	this.freakMatcher.addFreakFeaturesAndDescriptors(
            			fps,
            			dl,
                        p3v,
                        i_refDataSet.page_info[k].image_info[m].w,
                        i_refDataSet.page_info[k].image_info[m].h,
                        db_id++);
            }
        }
	    return 0;
	}
}
