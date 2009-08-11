package jp.nyatla.nyartoolkit.core.labeling.rlelabeling;


import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfo;
import jp.nyatla.nyartoolkit.core.labeling.NyARLabelInfoStack;


public class RleLabelFragmentInfoStack  extends NyARLabelInfoStack<RleLabelFragmentInfoStack.RleLabelFragmentInfo>
{
	public class RleLabelFragmentInfo extends NyARLabelInfo
	{
		//継承メンバ
		//int area; // フラグメントラベルの領域数
		public short id; // フラグメントラベルのインデクス
		public short entry_x; // フラグメントラベルの位置
		public short entry_y; // フラグメントラベルの位置
		public short clip_t;
		public short clip_l;
		public short clip_r;
		public short clip_b;
	}	
	public RleLabelFragmentInfoStack(int i_length)
	{
		super(i_length, RleLabelFragmentInfoStack.RleLabelFragmentInfo.class);
		return;
	}

	protected RleLabelFragmentInfoStack.RleLabelFragmentInfo createElement()
	{
		return new RleLabelFragmentInfoStack.RleLabelFragmentInfo();
	}
}