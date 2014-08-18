package nc.ui.so.so001.order;

import java.awt.Container;

import nc.ui.pub.ClientEnvironment;
import nc.ui.trade.controller.IControllerBase;
import nc.ui.xcgl.pub.bill.XCMBillSourceDLG;

public class BillSourceDlg extends XCMBillSourceDLG{
	private static final long serialVersionUID = -4908986743054244885L;

	public BillSourceDlg(String pkField, String pkCorp, String operator,
			String funNode, String queryWhere, String billType,
			String businessType, String templateId, String currentBillType,
			Container parent) {
		super(pkField, pkCorp, operator, funNode, queryWhere, billType, businessType,
				templateId, currentBillType, parent);
	}
	
	/**
	 * 主表查询条件
	 * @return
	 */
	public String getHeadCondition() {
		String whersql=" isnull(xcgl_soct.dr,0)=0 " +
				" and xcgl_soct.pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"'" +
			    " and xcgl_soct.vbillstatus=1 " +
			    " and xcgl_soct.isclose='N'";
		return whersql;
	}
	
	@Override
	public String getPk_invbasdocName() {
		return "invbasid";
	}

	@Override
	public String getPk_invmandocName() {
		return "invid";
	}


	@Override
	public IControllerBase getUIController() {
		return new nc.ui.xcgl.soct.Controller();
	}

	
}
