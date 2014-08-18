package nc.vo.so.so002;

import java.util.ArrayList;

import nc.bs.framework.common.NCLocator;
import nc.itf.scm.so.receive.IReceiveSplitQueryService;
import nc.ui.pub.para.SysInitBO_Client;
import nc.ui.scm.so.SaleBillType;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.pub.SCMEnv;
import nc.vo.scm.pub.session.ClientLink;
import nc.vo.so.pub.SOGenMethod;
import nc.vo.so.so002.SaleVO;
import nc.vo.so.so002.SaleinvoiceBVO;
import nc.vo.so.so002.SaleinvoiceVO;
import nc.vo.so.sodispart.SaleDispartVO;

public class SaleinvoiceSplitTools {
    /**
     * 方法功能描述：销售发票分单实现。
     * <b>参数说明</b>
     * @param invoicevos
     * @return
     * @author fengjb
     * @time 2009-9-10 下午07:32:55
     */
	public SaleinvoiceVO[] splitSaleinvoiceVOs(SaleinvoiceVO[] invoicevos) {
		//参数合法性校验
		if (null == invoicevos || invoicevos.length == 0)
			return invoicevos;
		// 分单后返回的VO
		SaleinvoiceVO[] aryRetVO = invoicevos;
		// 分单依据
		String[] headkeys = null;
		String[] bodykeys = null;

		//上层来源单据类型
		String sUpSrcType = invoicevos[0].getItemVOs()[0].getCupreceipttype();

		// 是否按照产品线进行合并开票
		UFBoolean SO_27 = getParaBoolean("SO27");
		if (null == SO_27)
			SO_27 = UFBoolean.FALSE;
		//上层来源销售订单
		if (SaleBillType.SaleOrder.equals(sUpSrcType)) {
			//主表开票客户、销售组织
			headkeys = new String[] { "creceiptcorpid", "csalecorpid" };

			//参照订单开票是否按照订单客户分单
			UFBoolean SO_60 = getParaBoolean("SO60");
			if (null == SO_60)
				SO_60 = UFBoolean.TRUE;
			if (SO_60.booleanValue() && SO_27.booleanValue())
				bodykeys = new String[] { "ccurrencytypeid", "ccustomerid",
						"cprolineid" };
			else if (SO_60.booleanValue())
				bodykeys = new String[] { "ccurrencytypeid", "ccustomerid" };
			else if (SO_27.booleanValue())
				bodykeys = new String[] { "ccurrencytypeid", "cprolineid" };
			else
				bodykeys = new String[] { "ccurrencytypeid" };
			aryRetVO = (SaleinvoiceVO[]) nc.vo.scm.pub.vosplit.SplitBillVOs
					.getSplitVOs("nc.vo.so.so002.SaleinvoiceVO",
							"nc.vo.so.so002.SaleVO",
							"nc.vo.so.so002.SaleinvoiceBVO", invoicevos,
							headkeys, bodykeys);
			//来源销售出库单
		} else if (SaleBillType.SaleOutStore.equals(sUpSrcType)) {
			//分单条件
			SaleDispartVO[] thisSplitCon = getSplitCondition(invoicevos);
			ArrayList<String> bodykey = new ArrayList<String>();
			//是否选择“实际出库日期时距”的分单条件
			boolean isoutstack = false;
			UFDouble limit = new UFDouble(0);

			//表体开票客户
			bodykey.add("h_creceiptcorpid");
			//表体销售组织
			bodykey.add("csalecorpid");
			//币种
			bodykey.add("ccurrencytypeid");
			//产品线
			if (SO_27.booleanValue()) {
				bodykey.add("cprolineid");
			}
			for (SaleDispartVO thiscon : thisSplitCon) {

				// 订单客户
				if ("ccustomerid".equals(thiscon.getDispartkey())) {
					if (thiscon.getBdefault().booleanValue())
						bodykey.add("ccustomerid");

					// 出库单号
				} else if ("cupsourcebillcode".equals(thiscon.getDispartkey())) {

					if (thiscon.getBdefault().booleanValue())
						bodykey.add("cupsourcebillcode");

				} else if ("d4cbizdate".equals(thiscon.getDispartkey())) {
					// 实际出库日期
					if (thiscon.getBdefault().booleanValue())
						bodykey.add("d4cbizdate");

				} else if ("datefromto".equals(thiscon.getDispartkey())) {
					// 实际出库单日期时距
					if (thiscon.getBdefault().booleanValue()
							&& thiscon.getValue() != null) {
						isoutstack = true;
						limit = thiscon.getValue();
					}
				}
				//liyu1 销售发票参照销售出库单希望增加按订单号进行分单
				else if("coriginalbillcode".equals(thiscon.getDispartkey())){
                    //订单号
					if (thiscon.getBdefault().booleanValue())
						bodykey.add("coriginalbillcode");
					
				}
			}

			bodykeys = bodykey.toArray(new String[0]);
			aryRetVO = (SaleinvoiceVO[]) nc.vo.scm.pub.vosplit.SplitBillVOs
					.getSplitVOs("nc.vo.so.so002.SaleinvoiceVO",
							"nc.vo.so.so002.SaleVO",
							"nc.vo.so.so002.SaleinvoiceBVO", invoicevos, headkeys,
							bodykeys);

			if (isoutstack) {
				aryRetVO = (SaleinvoiceVO[]) nc.vo.scm.pub.vosplit.SplitBillVOs
						.getSplitVOsByDate("nc.vo.so.so002.SaleinvoiceVO",
								"nc.vo.so.so002.SaleVO",
								"nc.vo.so.so002.SaleinvoiceBVO", aryRetVO,
								"d4cbizdate", limit.intValue());
			}
			
			fillHeadData(aryRetVO);
		}
         return aryRetVO;
	}
    /**
     * 方法功能描述：销售发票参照销售出库单生成时使用表体缓存数据填充表头。
     * <b>参数说明</b>
     * @param aryRetVOs
     * @author fengjb
     * @time 2009-9-10 下午07:32:00
     */
	private void fillHeadData(SaleinvoiceVO[] aryRetVOs) {
         //	销售组织、开票客户基本ID、默认客商开户银行、客户打印名称
		 //部门、业务员、收付款协议、是否散户、散户ID、整单折扣
		String[] headkeys = new String[]{
			"csalecorpid","ccubasid","ccustbankid"	,"vprintcustname",
			"cdeptid","cemployeeid","ctermprotocolid","bfreecustflag","cfreecustid","ndiscountrate"
		};
       for(SaleinvoiceVO invoicevo:aryRetVOs){
    	   SaleinvoiceBVO bvo = invoicevo.getBodyVO()[0];
    	   SaleVO head = invoicevo.getHeadVO();
    	   for(String key:headkeys){
    		   Object objvalue = bvo.getAttributeValue(key);
    		   head.setAttributeValue(key, objvalue);
    	   }
    	   //2009-11-12 冯加滨 陈恩宇 方婵 由于表头开票客户和表体收货单位字段重名，需要单独处理
    	   String h_creceiptcorpid = (String)bvo.getAttributeValue("h_creceiptcorpid");
    	   head.setCreceiptcorpid(h_creceiptcorpid);
    	   //发票表头收货单位取表体第一行收货单位值
    	   head.setCreceiptcustomerid(bvo.getCreceiptcorpid());
       }
	}

	/**
	 * 方法功能描述：返回UFBoolean型参数值。
	 * <b>参数说明</b>
	 * @return
	 * @author fengjb
	 * @time 2009-9-8 下午01:30:27
	 */
	private UFBoolean getParaBoolean(String para) {
		UFBoolean value = null;
		try {
			value = SysInitBO_Client.getParaBoolean(SOGenMethod.getBSCorp(),
					para);
		} catch (BusinessException e) {
			SCMEnv.out(e);
		}
		return value;
	}

	/**
	 * 方法功能描述：获取销售发票参照销售出库单时分单参数。
	 * <b>参数说明</b>
	 * 
	 * @return
	 * @author fengjb
	 * @param invoicevos 
	 * @time 2009-9-8 下午07:34:16
	 */
	private SaleDispartVO[] getSplitCondition(SaleinvoiceVO[] invoicevos) {
		SaleDispartVO[] splitconTo4C = (SaleDispartVO[]) invoicevos[0]
				.getHeadVO().getAttributeValue("SO76");
		if (null == splitconTo4C) {
			ClientLink clientlink = new ClientLink(SOGenMethod.getBSCorp(),
					SOGenMethod.getBSUser(), SOGenMethod.getBSDate(), null,
					null, null, null, null, null, false, null, null, null);
			try {
				IReceiveSplitQueryService bo = (IReceiveSplitQueryService) NCLocator
						.getInstance().lookup(
								IReceiveSplitQueryService.class.getName());

				splitconTo4C = bo.queryAll(clientlink,
						SaleBillType.SaleInvoice, SaleDispartVO.ONLY_SPLIT);
			} catch (BusinessException e) {

				SCMEnv.out(nc.vo.ml.NCLangRes4VoTransl.getNCLangRes().getStrByID("40060302", "UPT40060302-000055")+ e.getMessage());
			}
			//没有设置分单参数
			if (null == splitconTo4C)
				splitconTo4C = SaleDispartVO.getDefaultSaleDispartVO(
						clientlink, SaleBillType.SaleInvoice);

		}
		return splitconTo4C;
	}
}
