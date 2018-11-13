package com.harlan.utils;
import java.util.Random;


public class CCgenerator {
	String name = null;
	String[] firstname={"赵","钱","孙","李","周","吴","郑","王","冯","陈","卫","蒋","沈","韩","杨","朱","秦","尤","许","施","张","孔","曹","严","华","金","魏","陶","姜","戚","谢","邹","柏","水","窦","章","云","苏","潘","范","彭","鲁","韦","马","凤","花","方","俞","任","袁","鲍","史","唐","费","廉","岑","薛","贺","倪","汤","罗","毕","郝","常","乐","于","时","齐","康","伍","孟","平","黄","穆","萧","尹","姚","计","成","谈","宋","庞","纪","舒","屈","项","祝","董","梁","阮","蓝","闵","席","季","贾","路","娄","江","童","颜","郭","梅","盛","林","钟","徐","邱","骆","高","夏","田","樊","胡","凌","霍","虞"};
	String[] lastname={"邦","福","歌","国","和","康","澜","民","宁","平","然","顺","翔","晏","宜","怡","易","志","然","昂","雄","宾","白","鸿","实","彬","炳","彬","郁","斌","蔚","海","光","波","鸿","峻","涛","博","瀚","超","达","厚","简","明","容","赡","涉","实","涛","文","学","雅","延","艺","易","裕","远","捷","良","艺","英","哲","俊","成","和","弘","化","济","礼","龙","仁","双","天","业","益","荫","周","安","承","弼","德","恩","福","基","教","平","嗣","望","宣","颜","业","悦","允","运","载","泽","志","海","厚","华","辉","惠","容","润","寿","馨","曜","业","义","庸","佑","宇","元","运","泽","明","飞","飙","掣","尘","沉","驰","光","翰","航","翮","鸿","虎","捷","鹏","扬","文","翔","星","翼","英","宇","羽","雨","语","跃","章","舟","华","茂","羽","芬","歌","格","寒","翰","杰","洁","峻","朗","丽","邈","旻","明","爽","兴","轩","雅","扬","阳","义","谊","逸","懿","原","远","韵","卓","赫","济","霁","亮","临","启","熙","誉","远","晗","昱","畅","涤","容","涵","蓄","衍","意","映","育","采","池","瀚","玥","翰","藻","海","苍","昊","空","乾","穹","然","昊","焱","英","浩","波","初","宕","荡","涆","慨","旷","阔","漫","淼","渺","邈","穰","壤","思","言","蔼","安","璧","昶","惬","顺","硕","颂","泰","悌","通","同","煦","雅","宜","怡","玉","裕","豫","悦","韵","泽","正","志","鹤","轩","弘","博","厚","济","量","深","盛","图","义","益","毅","致","壮","宏","博","富","峻","浚","恺","旷","朗","邈","儒","深","胜","鸿","宝","彩","畴","达","晖","羲","禧","信","轩","煊","雪","哲","祯","卓","奥","采","彩","灿","藏","池","赐","慕","纳","年","平","瑞","胜","石","实","树","澍","祥","歆","勋","言","谊","佑","誉","悦","元","章","中","健","柏","鑫","锦","程","瑾","瑜","赋","亘","纶","纬","武","澄","铄","捷","郎","晤","贤","雄","彦","逸","喆","智","霁","康","旋","泽","裕","力","夫","勤","行","果","骥","典","敏","叡","彭","勃","薄","湃","魄","越","泽","祖","池","鲸","举","鹍","鲲","濮","存","溥","璞","瑜","浦","奇","略","迈","邃","致","祺","祥","瑞","琪","睿","锐","达","锋","进","精","立","利","藻","睿","博","慈","聪","绍","辉","绍","钧","晟","斯","材","成","罡","骄","禄","佑","甫","巍","奕","晔","温","纶","韦","文","柏","星","向","晨","笛","翔","宇","项","禹","新","觉","鸥","火","剑","津","阑","渊","洲","修","洁","谨","筠","竹","贤","尧","炫"};
	
	public CCgenerator(){
	}
	
	public String getName() {
		Random random = new Random();
		String temp = new String();
		int firstnamenum = random.nextInt(firstname.length);
		temp+=firstname[firstnamenum];
		int lastnametimes = random.nextInt(2);
		int times=0;
		
		if (lastnametimes>1) {
			times=2;
		}else{
			times=1;
		}
		
		for (int i = 0; i < times; i++) {
			int lastnamenum = random.nextInt(lastname.length);
			temp += lastname[lastnamenum];
		}
//System.out.println("Name = " + temp);		
		return temp;
	}

}