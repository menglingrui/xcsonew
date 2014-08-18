package nc.ui.so.so002;


import java.awt.Container;

import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.pf.IinitQueryData2;
import nc.ui.trade.query.HYQueryDLG;
import nc.vo.xcgl.pub.consts.PubNodeCodeConst;
/**
 * re
 * @author mlr
 *
 */
public class BillQueryTxDlg extends HYQueryDLG implements IinitQueryData2{
	private static final long serialVersionUID = 2955002897509988510L;
	static Container parent = null;
	static UIPanel normalPnl = null;
	static String pk_corp =  ClientEnvironment.getInstance().getCorporation().getPrimaryKey();
	static String moduleCode =  null;
	static String operator = ClientEnvironment.getInstance().getUser().getPrimaryKey();
	static String busiType =  null;
	static String nodeKey =  null;
	
	public  BillQueryTxDlg(Container parent) {
		super(parent,null,pk_corp,PubNodeCodeConst.NodeCode_salesettledroop,operator,null,"REF");
	}
	
	public BillQueryTxDlg(Container parent, UIPanel normalPnl, String pk_corp,
			String moduleCode, String operator, String busiType, String nodeKey) {
		super(parent, normalPnl, pk_corp, moduleCode, operator, busiType, nodeKey);
	}


	public BillQueryTxDlg(Container parent, UIPanel normalPnl, String pk_corp,
			String moduleCode, String operator, String busiType) {
		super(parent, normalPnl, pk_corp, moduleCode, operator, busiType);
	}
	
	@Override
	public String getWhereSQL() {
	
		return super.getWhereSQL();
	}

	public void initData(String pkCorp, String operator, String funNode,
			String businessType, String currentBillType, String sourceBilltype,
			String nodeKey, Object userObj) throws Exception {
	}
}
