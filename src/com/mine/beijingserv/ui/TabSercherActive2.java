package com.mine.beijingserv.ui;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mine.beijingserv.R;
import com.mine.beijingserv.model.FirstCatalogue;
import com.mine.beijingserv.model.MessageInfo;
import com.mine.beijingserv.model.SecondCatalogue;
import com.mine.beijingserv.sys.AppContex;
import com.mine.beijingserv.sys.DBUtil;
import com.mine.beijingserv.sys.SysUtils;
import com.mine.beijingserv.sys.ToastShow;
import com.mine.beijingserv.sys.UpdateMessageState;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TabSercherActive2 extends Activity implements
		View.OnClickListener, AdapterView.OnItemClickListener {

	private AlertDialog quiteAlertDialog = null;
	private EditText search_bar;
	private String keyWordsString;
	private TextView serch_Commitbutton;
	private ExpandableListView yujingtypelistview;
	private ListView serch_message_list;
	private MessageAdapter messageAdapter;
	private ArrayList<String> yujingArrayList = new ArrayList<String>();
	private Vector<MessageInfo> searchedInfos = new Vector<MessageInfo>();
	private SearchThread searchThread;
	private ImageView alertImage;

	public static String[] yujingSubArrayStrings = { 
		
			"暴雨红色预警信号","暴雨橙色预警信号", "暴雨黄色预警信号", "暴雨蓝色预警信号", 
			"雷电红色预警信号", "雷电橙色预警信号",	"雷电黄色预警信号", "高温红色预警信号", "高温橙色预警信号",
			"高温黄色预警信号","高温蓝色预警信号", "大风红色预警信号","大风橙色预警信号",
			"大风黄色预警信号", "大风蓝色预警信号",  "霾红色预警信号",
			"霾橙色预警信号", "霾黄色预警信号",
			"持续低温黄色预警信号", "持续低温蓝色预警信号", "电线积冰橙色预警信号",
			"电线积冰黄色预警信号", "道路结冰红色预警信号", "道路结冰橙色预警信号", "道路结冰黄色预警信号", "大雾红色预警信号",
			"大雾橙色预警信号", "大雾黄色预警信号", "霜冻橙色预警信号", "霜冻黄色预警信号", "霜冻蓝色预警信号",
			"冰雹红色预警信号", "冰雹橙色预警信号", "冰雹黄色预警信号", "干旱红色预警信号", "干旱橙色预警信号",
			 "沙尘暴红色预警信号", "沙尘暴橙色预警信号", "沙尘暴黄色预警信号", 
			 "寒潮红色预警信号", "寒潮橙色预警信号","寒潮黄色预警信号", "寒潮蓝色预警信号", 
			 "暴雪红色预警信号", "暴雪橙色预警信号", "暴雪黄色预警信号","暴雪蓝色预警信号",
			"台风红色预警信号", "台风橙色预警信号", "台风黄色预警信号", "台风蓝色预警信号" };

	public static String[] yujingSubContentArray = {
			"标准：预计未来可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）1小时降雨量达60毫米以上；/n（2）3小时降雨量达100毫米以上；/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责及时做好城区、郊区县及山区暴雨及其次生灾害的应急防御和抢险工作；面向社会滚动发布灾情、灾害风险和旅游风险信息； /n2. 交通管理部门应实施高级别交通管制，确保深积水路面、地面塌陷、洪水冲毁、高压线塔倒塌、电杆倒折、高压线垂地等危险区域有明确标识和专人值守，严禁车辆及行人进入；/n3. 停止集会，停课、停业（除特殊行业外）；/n4. 驾驶人员应听从民警指挥，切勿涉入积水不明路段；汽车如陷入深积水区，应迅速下车转移；/n5. 个人不要外出；如在野外，可选地势较高的民居暂避，不要在山梁或山顶上行走，以防雷击，也不要沿山谷低洼处行走，提防山洪、滑坡、泥石流； /n6. 居住在病险水库下游、山体易滑坡地带、泥石流多发区、低洼地区、有结构安全隐患房屋等危险区域人群要迅速转移到安全区域。",
			"标准：预计未来可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）1小时降雨量达40毫米以上；/n（2）3小时降雨量达50毫米以上；/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责启动防暴雨应急工作，做好城区与郊县河道、道路与排水管道的清淤、疏通，注意防范山洪、滑坡、泥石流等灾害；/n2. 交通管理部门应当根据暴雨灾害和道路情况，分片分段强化交通管控，设立交通警示标志，疏导交通堵塞；/n3. 受暴雨洪涝威胁的危险地带应停止集会、停课、停业，采取专门措施保护在校学生、幼儿和上班人员的安全；/n4. 驾驶人员应暂停行驶，将车停靠在地势较高处或安全位置，人员到高处躲避；/n5. 个人避免外出，如需出行应搭乘公共交通工具；山区人员要防范山洪，避免渡河，不要沿河床行走，注意山体滑坡、滚石、泥石流；如发现高压线铁塔倾倒、电线低垂或断折，要远离避险，不可触摸或接近。 /n6. 可在低洼地区房屋门口放置挡水板、沙袋或土坎，地下设施（如地铁）的地面入口要砌好沙袋，严防雨水倒灌；有雨水漫入室内时，应立即切断电源；危旧房及山洪地质灾害易发区内人员应及时转移到安全地点。",
			"标准：预计未来可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）1小时降雨量达30毫米以上；/n（2）6小时降雨量达50毫米以上；/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防暴雨工作，检查城市、农田以及其它重要设施的排水系统，及时清理排水管道，做好排涝工作；/n2. 交通管理部门应根据路况，加密交通信息提示，在强降雨路段采取交通管制措施，在积水路段实行交通引导；/n3. 中小学、幼儿园可提前或推迟上下学时间，采取防护措施，确保学生、幼儿上下学及在校安全；/n4. 驾驶人员要及时了解交通信息和前方路况，遇到路面或立交桥下积水过深，应尽量绕行，避免强行通过。/n5. 行人应避开桥下（尤其是下凹式立交桥下）、涵洞等低洼地区，切勿在高楼、广告牌下躲雨或停留；在积水中行走时，要注意观察路面情况。/n6．检查电路、炉火、煤气阀等设施是否安全，切断低洼地带有危险的室外电源，暂停在空旷地方的户外作业，危险地带人员和危房居民转移到安全场所避雨。",
			"标准：预计未来可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）1小时降雨量达20毫米以上；/n（2）3小时降雨量达30毫米以上；/n（3）12小时降雨量达50毫米以上；/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防暴雨准备工作，检查城市、农田以及其它重要设施排水系统，做好排涝准备。/n2. 小学和幼儿园学生上、下学应由成人带领，采取适当措施，保证学生和幼儿的安全；/n3. 驾驶人员应当注意道路积水和交通阻塞，确保行车安全；/n4. 行人不要在高楼或大型广告牌下躲雨、停留，以免被坠落物砸伤；/n5. 家庭和个人应检查电路、炉火等设施是否安全。",
			"标准：2小时内可能发生雷电活动并伴有8级以上短时大风；或者已经有强烈雷电及8级以上短时大风发生，并可能持续，出现雷电和大风灾害事故的可能性非常大。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防雷应急抢险工作；/n2. 公园、游乐场等露天场所应停运户外设施，并疏导游人到安全场所；/n3. 停止所有户外活动，及时躲避到有防雷装置的建筑物；/n4. 不要在大树下避雨，远离高塔、烟囱、电线杆、广告牌等高耸物，不要停留在山顶、山脊、楼顶、水边或空旷地带，不宜使用手机, 切勿接触天线、水管、铁丝网、金属门窗、建筑物外墙，远离电线等带电设备和其他类似金属装置；/n5. 在空旷场地不要打伞，不要把农具、羽毛球拍、高尔夫球杆尤其是带金属的物体等扛在肩上，应在地势较低地方下蹲，降低身体高度；/n6. 室内人员应关好门窗并保持安全距离，不要触碰水管、燃汽、暖气等金属管道，切勿洗澡，避免使用固定电话、电脑、电视等电器设备；/n7. 对被雷击中人员，应立即采用心肺复苏法抢救，同时将病人速送医院；发生雷击火灾应立刻切断电源，并迅速拨打报警电话，不要带电泼水。",
			"标准：2小时内可能发生雷电活动并伴有6级以上短时大风；或者已经有雷电及6级以上短时大风发生，并可能持续，出现雷电和大风灾害事故的可能性很大。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责落实防雷应急措施；/n2. 公园、游乐场等露天场所应停运户外设施，并疏导游人到安全场所；/n3. 停止户外运动或作业，及时躲避到有防雷装置的建筑物；/n4. 不要在大树下避雨，远离高塔、烟囱、电线杆、广告牌等高耸物，不要停留在山顶、山脊、楼顶、水边或空旷地带，不宜使用手机；/n5. 在空旷场地不要打伞，不要把农具、羽毛球拍、高尔夫球杆尤其是带金属的物体等扛在肩上，应在地势较低地方下蹲，降低身体高度；/n6. 室内人员应关好门窗并保持安全距离，不要触碰水管、燃汽、暖气等金属管道，切勿洗澡，避免使用固定电话、电脑、电视等电器设备。",
			"标准：6小时内可能发生雷电活动（并伴有短时大风），有可能出现雷电灾害事故。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按职责做好防雷工作，组织检查雷击隐患单位或部门；/n2. 公园、游乐场等露天场所应停运户外设施，并疏导游人到安全场所；/n3. 应停止登山、体育、农作、游泳、钓鱼等户外活动(运动)，及时躲避到有防雷装置的建筑物。",
			"标准：预计未来可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）连续三天平原地区日最低气温低于零下12℃；/n（2）连续三天平原地区日平均气温比常年同期（气候平均值）偏低7℃及以上。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防御低温准备工作；/n2. 农、林、养殖业做好作物、树木防冻害与牲畜防寒准备；设施农业生产企业和农户注意温室内温度的调控，防止蔬菜和花卉等经济植物遭受冻害；/n3．有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 户外长时间作业和活动人员应采取必要的防护措施；/n5. 个人外出注意戴帽子、围巾和手套，早晚期间要特别注意防寒保暖。",
			"标准：24小时最高气温将升至40℃以上。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责启动和实施防暑降温应急措施，密切关注保障整个城市安全运行的各项工作；/n2. 供电部门防范用电量过高及电线变压器等电力负载过大而引发的事故，消防部门加大值班警力投入，有关部门和单位都要特别注意防火；/n3. 高温时段停止户外露天作业（除特殊行业外）和户外活动，中小学、幼儿园在高温时段停课休息；/n4. 驾驶人员要保证睡眠充足，避免疲劳驾驶；车内勿放易燃物品，开车前应检查车况、水箱和电路，严防车辆自燃；/n5. 加强对老、弱、病、幼特别是高血压、心肺疾病患者的照料护理，如有胸闷、气短等症状应及时就医；/n6. 高温时段不进行户外活动，出行避开中午和午后，采取有效的遮阳防晒防护措施；/n7. 高温时期应备好防暑降温药品，多饮用凉白开、冷盐水等防暑饮品；室内空调的温度不宜过低，节约用水用电。",
			"标准：24小时最高气温将升至37℃以上。/n防御指南： /n1. 地方各级人民政府、有关部门和单位按照职责落实防暑降温保障措施，市政、公安、建筑、电力、卫生等部门与单位应立即采取措施，保障生产、消防、卫生安全和城市供水、供电；/n2. 高温时段避免剧烈运动和高强度作业，高温条件下作业的人员应当缩短连续工作时间，必要时停止生产作业；/n3. 驾驶人员要保证睡眠充足，避免疲劳驾驶；车内勿放易燃物品，开车前应检查车况，严防车辆自燃；/n4. 注意对老、弱、病、幼特别是高血压、心肺疾病患者的照料护理，如有胸闷、气短等症状应及时就医；/n5. 避免长时间户外活动，合理安排外出活动时间，避开中午和午后，外出采取有效的遮阳防晒措施；/n6. 高温高湿条件下人易疲倦，要合理调整作息时间，中午适当休息，保持良好心态。",
			"标准：连续三天日最高气温将在35℃以上。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按职责做好防暑降温工作，市政、水务、建筑、卫生、电力等部门与单位应及时采取有效的应对措施；/n2. 高温环境下长时间进行露天作业人员应当采取必要的防暑降温措施，备好清凉饮料和中暑急救药品；/n3. 对汽车进行合理养护，开车注意交通安全，避免疲劳驾驶；/n4. 有老、弱、病、幼的家庭应备好常用的防暑降温药品，并提供防暑降温指导及一定的照料；/n5. 高温时段应减少户外活动，必须出行的带好防晒用具，在户外要打遮阳伞，戴遮阳帽和太阳镜，涂抹防晒霜，避免强光灼伤皮肤； /n6. 持续高温天气容易使人疲倦、烦躁和发怒，应注意调节情绪，保证充分休息。",
			"标准：连续两天日最高气温将在35℃以上。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防暑降温准备工作，市政、水务、电力、等部门与单位注意采取适当应对措施；/n2. 高温环境下长时间进行户外露天作业的人员应采取必要的防护措施；/n3. 高温时段尽量减少户外活动；必须外出时，应在行前做好防晒准备，备好遮阳物和防暑药品、饮用水；/n4. 对老、弱、病、幼人群提供防暑降温指导；注意饮食卫生和适当休息，不长时间吹空调，浑身大汗时不宜冲凉水澡。",
			"标准：6小时可能受大风影响，平均风力可达12级以上，或者阵风13级以上；或者已经受大风影响，平均风力为12级以上，或者阵风13级以上并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防大风应急和抢险工作，做好全市防火工作，机场、铁路和交通管理部门应立即实施交通管制；/n2. 停止一切露天活动，中小学校和有关单位针对强风时段适时停课、停业，躲避风灾；/n3. 切断户外危险电源，立即疏散、转移危险地带和危房居民； /n4. 驾驶人员立刻将车辆停靠在安全地带，并到安全场所避风；/n5. 室内人员应当尽可能停留在防风安全的地方，关好门窗，并在窗玻璃上贴上米字形胶条，防止玻璃破碎，并远离窗口，以免强风席卷沙石击破玻璃伤人；户外人员及时到安全场所躲避。",
			"标准：6小时可能受大风影响,平均风力可达10级以上，或者阵风11级以上；或者已经受大风影响, 平均风力为10～11级，或者阵风11～12级并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责启动防大风应急工作，做好森林、草场和城区等防火工作，机场、铁路和交通管理部门应采取的交通管制措施，保障交通安全；/n2. 停止高空、水上户外作业和一切露天集体活动，房屋抗风能力较弱的中小学校和单位应当停课、停业；/n3. 切断户外危险电源，加固围板、棚架、广告牌等易被大风吹动的搭建物，妥善安置易受大风影响的室外物品，疏散、转移危险地带和危房居民；/n4. 车辆减速慢行，转弯时要小心控制车速，防止侧翻，不要停在高楼、大树等下方；/n5. 人员减少外出，老人和小孩不要外出；外出人员不要在高大建筑物、广告牌、临时搭建物或大树的下方停留。",
			"标准：12小时可能受大风影响,平均风力可达8级以上，或者阵风9级以上；或者已经受大风影响, 平均风力为8～9级，或者阵风9～10级并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防大风工作，做好森林、草场和城区防火，机场、铁路和交通管理部门应采取适度交通管制，保障交通安全；/n2. 停止高空、水上户外作业和游乐活动；停止露天集会，并疏散人员；/n3. 切断户外危险电源，加固围板、棚架、广告牌等易被大风吹动的搭建物，妥善安置易受大风影响的室外物品； /n4. 车辆减速慢行，不要停在高楼、大树等下方；  /n5. 外出时避免骑自行车，不要在高大建筑物、广告牌、临时搭建物或大树的下方停留。",
			"标准：24小时可能受大风影响,平均风力可达6级以上，或者阵风7级以上；或者已经受大风影响, 平均风力为6～7级，或者阵风7～8级并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防大风准备工作，密切关注森林、草场和城区防火，机场、铁路和交通管理部门应采取措施保障交通安全；/n2. 停止高空、水上户外作业和游乐活动；/n3. 加固围板、棚架、广告牌等易被大风吹动的搭建物，妥善安置易受大风损坏的室外物品；检查大棚薄膜，粘补漏洞，暂停农田灌溉；/n4. 行人尽量少骑自行车，在施工工地附近行走时应尽量远离工地并快速通过；行人与车辆不要在高大建筑物、广告牌、临时搭建物或大树的下方停留；/n5. 街道、社区、村庄和家庭应加强防火意识，适时采取有效措施，消除火灾隐患。",
			"标准：2小时可能出现强浓雾天气，能见度小于50米；或者已经出现能见度小于50米的雾并可能持续。/n防御指南：/n1. 有关部门和单位按照职责做好防大雾应急工作；/n2. 机场、高速公路及城市交通管理部门应按照行业规定适时采取交通安全管制措施，并及时发布飞机停飞、公路封闭信息；/n3. 减少开车外出；必须驾车时，驾驶人员应开启雾灯和双闪，减速慢行，与前车保持足够的制动距离；/n4. 大雾天空气质量很差，不要进行户外活动，外出时带上口罩，老人、儿童和心肺病人不要外出，中小学停止户外体育课；/n5. 外出回来后，第一时间清洗面部及裸露的皮肤。",
			"标准：6小时可能出现浓雾天气，能见度小于200米；或者已经出现能见度小于200米、大于等于50米的雾并可能持续。/n防御指南：/n1. 有关部门和单位按照职责做好防大雾工作；/n2. 机场、高速公路及城市交通管理部门加强交通调度指挥；/n3. 机场和高速公路可能因大雾停航或封闭，出行前应查清路况、航班信息，调整出行的计划； /n4. 驾驶人员应及时开启雾灯，减速慢行，保持车距；/n5. 大雾天空气质量差，应减少户外活动，暂停晨练，外出应带上口罩，老人、儿童和心肺病人不要外出，中小学停止户外体育课； /n6. 外出回来后，立即清洗面部及裸露的皮肤。",
			"标准：12小时可能出现浓雾天气，能见度小于500米；或者已经出现能见度小于500米、大于等于200米的雾并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防雾准备工作；/n2. 机场、高速公路及城市交通管理部门应釆取管控措施，保障交通安全； /n3. 出行前应关注交通信息，驾驶人员注意雾的变化，小心驾驶；/n4. 雾天空气质量较差，不宜晨练，应尽量减少户外活动，出门最好带上口罩，老人、儿童和心肺病人不宜外出；/n5. 外出回来后，及时清洗面部及裸露的皮肤。",
			"标准：预计未来可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）连续三天平原地区日最低气温低于零下10℃；/n（2）连续三天平原地区日平均气温比常年同期（气候平均值）偏低5℃及以上。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防御低温准备工作；/n2. 农、林、养殖业做好作物、树木防冻害与牲畜防寒准备；设施农业生产企业和农户注意温室内温度的调控，防止蔬菜和花卉等经济植物遭受冻害；/n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 户外长时间作业人员应采取必要的防护措施；/n5. 个人外出应注意做好防寒保暖措施。",
			"标准：出现降雪、雾凇、雨凇等天气后遇低温出现严重电线积冰，预计未来24小时仍将持续，可能对电网有影响。/n防御指南：/n1. 电力及有关部门按职责做好电线积冰的防御工作；/n2. 加强对输电线路等重点设备、设施的检查和检修，确保正常，加强对应急物资、装备的检查。/n3. 车辆和人员不宜在有积冰的电线与铁塔下停留或走动、驾驶，以免冰棱砸落。",
			"标准：出现降雪、雾凇、雨凇等天气后遇低温出现电线积冰，预计未来24小时仍将持续。/n防御指南：/n1. 电力及有关部门按职责做好电线积冰的防御工作；/n2. 车辆和人员不宜在有积冰的电线与铁塔下停留或走动、驾驶，以免冰棱砸落。",
			"标准：当路表温度低于0℃，出现冻雨或雨雪，2小时内可能出现或者已经出现道路结冰，对交通有很大影响。/n防御指南：/n1. 交通、公安等部门做好道路结冰应急和抢险工作；/n2. 交通、公安等部门注意指挥和疏导行驶车辆，必要时关闭结冰道路；机场和公路管理单位积极采取破冰、融冰措施；/n3. 驾驶人员必须采取防滑措施，安装轮胎防滑链或给轮胎适当放气，听从交警指挥，慢速行驶，不超车、加速、急转弯或紧急制动，停车时多用换挡，少制动，防止侧滑；/n4. 人员尽量减少外出，必须外出时尽量乘坐公共交通工具，注意远离、避让车辆；老、弱、病、幼人员不要外出；/n5. 机场、高速公路可能会停航或封闭，出行前应注意查询路况与航班信息。",
			"标准：当路表温度低于0℃，出现冻雨或雨雪，6小时内可能出现道路结冰，对交通有较大影响。/n防御指南：/n1. 交通、公安等部门按照职责做好道路结冰应急工作，注意指挥和疏导行使车辆；/n2. 驾驶人员必须采取防滑措施，安装轮胎防滑链或给轮胎适当放气，听从交警指挥，慢速行驶，不超车、加速、急转弯或紧急制动，停车时多用换挡，少制动，防止侧滑；/n3. 行人外出尽量乘坐公共交通工具，注意远离、避让车辆；老、弱、病、幼人员避免外出，出行需有人陪同； /n4. 机场、高速公路可能会停航或封闭，出行前应注意查询路况与航班信息。",
			"标准：当路表温度低于0℃，出现雨雪，24小时内可能出现道路结冰，对交通有影响。/n防御指南：/n1. 交通、公安等部门按照职责做好道路结冰应对准备工作；/n2. 驾驶人员应注意路况，减速慢行； /n3. 行人外出尽量乘坐公共交通工具，少骑自行车或电动车，注意远离、避让车辆； 老、弱、病、幼人员尽量减少外出。",
			"标准：预计未来24小时内可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）能见度小于1000米且相对湿度小于80%的霾。/n（2）能见度小于1000米且相对湿度大于等于80%，PM2.5浓度大于250微克/立方米且小于等于500微克/立方米。/n（3）能见度小于5000米，PM2.5浓度大于500微克/立方米。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防霾应急工作；/n2. 排污单位采取措施，控制污染工序生产，减少污染物排放；/n3. 停止室外体育赛事；幼儿园和中小学停止户外活动；/n4. 停止户外活动，关闭室内门窗，等到预警解除后再开窗换气；儿童、老年人和易感人群留在室内；/n5. 尽量减少空调等能源消耗，驾驶人员减少机动车日间加油，停车时及时熄火，减少车辆原地怠速运行；/n6. 外出时戴上口罩，尽量乘坐公共交通工具出行，减少小汽车上路行驶；外出归来，立即清洗唇、鼻、面部及裸露的肌肤。",
			"标准：预计未来24小时内可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）能见度小于3000米且相对湿度小于80%的霾。/n（2）能见度小于3000米且相对湿度大于等于80%，PM2.5浓度大于115微克/立方米且小于等于150微克/立方米。/n（3）能见度小于5000米，PM2.5浓度大于150微克/立方米且小于等于250微克/立方米。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防霾准备工作；/n2. 排污单位采取措施，控制污染工序生产，减少污染物排放；/n3. 幼儿园与学校停止户外体育课；/n4. 减少户外活动和室外作业时间，避免晨练；缩短开窗通风时间，尤其避免早、晚开窗通风；老人、儿童及患有呼吸系统疾病的易感人群应留在室内，停止户外运动；/n5. 外出时最好戴口罩，尽量乘坐公共交通工具出行，减少小汽车上路行驶；/n6.外出归来，应清洗唇、鼻、面部及裸露的肌肤。 ",
			"标准：预计未来24小时内可能出现下列条件之一或实况已达到下列条件之一并可能持续：/n（1）能见度小于3000米且相对湿度小于80%的霾。/n（2）能见度小于3000米且相对湿度大于等于80%，PM2.5浓度大于115微克/立方米且小于等于150微克/立方米。/n（3）能见度小于5000米，PM2.5浓度大于150微克/立方米且小于等于250微克/立方米。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防霾准备工作；/n2. 排污单位采取措施，控制污染工序生产，减少污染物排放；/n3. 幼儿园与学校停止户外体育课；/n4. 减少户外活动和室外作业时间，避免晨练；缩短开窗通风时间，尤其避免早、晚开窗通风；老人、儿童及患有呼吸系统疾病的易感人群应留在室内，停止户外运动；/n5. 外出时最好戴口罩，尽量乘坐公共交通工具出行，减少小汽车上路行驶；/n6.外出归来，应清洗唇、鼻、面部及裸露的肌肤。 ",
			"标准：24小时地面最低温度将要下降到零下5℃以下，对农业将产生严重影响，或者已经降到零下5℃以下，对农业已经产生严重影响，并将持续。/n防御指南：/n1. 政府及农林主管部门按照职责做好防霜冻应急工作；/n2. 农业部门及其有关单位要抓紧时间，组织防霜抗灾，避免和减少损失；/n3. 对农作物及时采取覆盖、熏烟、灌溉等防冻措施，以避免和减少损失；要对瓜菜育苗温室大棚夜间要严密覆盖，早晨推迟揭帘；/n4. 农村基层组织和农户、农民要因地制宜地及时对蔬菜、花卉、瓜果等经济作物和大田作物采取灌溉、喷施抗寒制剂、人工烟熏、覆盖地膜等措施；/n5. 对发生春霜冻危害的作物，要根据受冻程度分别采取加强水肥受理、补种补栽、毁种改种等补救措施；对秋霜冻受害作物，可利用部分及时收获，不可利用部分及时处理。",
			"标准：24小时地面最低温度将要下降到零下3℃以下，对农业将产生严重影响，或者已经降到零下3℃以下，对农业已经产生严重影响，并可能持续。/n防御指南：/n1. 政府及农林主管部门按照职责做好防霜冻应急工作；/n2. 农业部门及其有关单位应抓住最佳时段，发动农村基层组织防霜抗灾，避免和减少损失；/n3. 蔬菜育苗温室和大棚夜间应覆盖草帘；菜苗瓜苗移栽和喜温作物春播应推迟到霜冻结束后进行；/n4. 农村基层组织和农户、农民要适时对蔬菜、花卉、瓜果等经济作物采取增温、覆盖、熏烟、喷雾、喷洒防冻液等措施，减轻冻害。",
			"标准：48小时地面最低温度将要下降到0℃以下，对农业将产生影响，或者已经降到0℃以下，对农业已经产生影响，并可能持续。/n防御指南：/n1. 政府及农林主管部门按照职责做好防霜冻准备工作；/n2. 农业部门及其有关单位应及时组织群众防霜，避免和减少损失；/n3. 对农作物、蔬菜、花卉、瓜果、林业育种应采取覆盖、灌溉等防护措施，加强对瓜菜苗床的保护；/n4. 农村基层组织和农户应关注当地霜冻预警信息，以便采用有针对性的防霜防冻措施，避免冻害损失。",
			"标准：2小时内出现冰雹可能性极大，并可能造成重雹灾。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防冰雹的应急和抢险工作，气象部门适时开展人工防雹作业；/n2. 停止所有户外活动，疏导人员到安全场所；中小学、幼儿园采取防护措施，确保学生、幼儿上下学及在校安全；/n3. 行车途中如遇降雹，应在安全处停车，坐在车内静候降雹停止；/n4. 人员切勿外出，确保老人、小孩待在家中；户外行人立即到安全的地方躲避； /n5. 室内要紧闭门窗，保护并安置好易受冰雹、雷电、大风影响的室外物品；车辆停放在车库等安全位置；及时驱赶家禽、牲畜进入有顶蓬的场所； /n6. 雷电常伴随冰雹同时发生，户外人员不要进入孤立的建筑物，或在高楼、烟囱、电线杆与大树下停留，应到坚固又防雷处躲避。",
			"标准：6小时内可能出现冰雹天气，并可能造成雹灾。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防冰雹的应急工作；气象部门做好人工防雹作业准备并择机进行作业；/n2. 户外作业人员应暂时停工，到室内暂避；小学、幼儿园暂停户外活动，确保学生、幼儿上下学及在校安全；/n3. 妥善保护易受冰雹袭击的室外物品或设备, 将汽车停放在车库等安全位置；/n4. 人员避免外出，保证老人、小孩待在家中；户外行人到安全的地方暂避； /n5. 雷电常伴随冰雹同时发生，户外人员不要进入孤立的建筑物，或在高楼、烟囱、电线杆与大树下停留，应到坚固又防雷处躲避。",
			"标准：6小时内可能或已经在部分地区出现分散的冰雹，可能造成一定的损失。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好冰雹防御应对工作；气象部门启动人工防雹作业准备并择机进行作业；/n2. 加强农作物防护措施，妥善保护易受冰雹袭击的汽车等室外物品或者设备；/n3. 人员不要随意外出，户外行人注意到安全的地方暂避，不要呆在室外或空旷的地方；野外行车应尽快停靠在可躲避处；/n4. 注意防御冰雹天气伴随的雷电灾害。",
			"标准：预计未来一周综合气象干旱指数达到特旱(气象干旱为50年以上一遇)，或者某一县（区）有60%以上的农作物受旱。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防御干旱的应急和救灾工作，确保供电安全，实施综合性抗旱措施；/n2. 各级政府和有关部门启动远距离调水等应急供水方案，采取提外水、打深井、车载送水等多种手段，确保城乡居民生活和牲畜饮水；/n3. 气象部门适时加大人工增雨作业力度；/n4. 加强水资源调节力度，控制小水电站发电用水，加强雨水收集和再生水的开发利用；/n5. 缩小或者阶段性停止农业灌溉供水，并做好灾后补救性生产准备；/n6. 严禁非生产性高耗水及服务业用水，停止排放工业污水；/n7. 家庭和居民应特别注意节约用水。",
			"标准：预计未来一周综合气象干旱指数达到重旱(气象干旱为25～50年一遇)，或者某一县（区）有40%以上的农作物受旱。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责启动和做好防御干旱的应急工作，保持电力系统正常运行，启动抗旱措施；/n2. 有关部门启用应急备用水源，调度辖区内一切可用水源，优先保障城乡居民生活用水和牲畜饮水；/n3. 气象部门适时进行人工增雨作业；/n4. 压减城镇供水指标，限制非生产性高耗水及服务业用水（如洗车），限制排放工业污水；/n5. 优先保证保护地、经济作物与高产地块的灌溉用水，限制粗放型、高耗水作物灌溉用水，鼓励开展滴灌和喷洒抗旱技术；/n6. 家庭和个人注意节约用水。",
			"标准：6小时可能出现特强沙尘暴天气，能见度小于50米；或者已经出现特强沙尘暴天气并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按职责做好防沙尘暴应急和抢险工作，交通、卫生等部门与单位应立即采取相应的交通管控和卫生安全的行动，有关部门和单位做好森林、草场和城区防火工作；/n2. 停止户外作业和露天活动；学校、幼儿园推迟上下学，必要时停课；/n3. 飞机暂停起降，火车暂停运行，高速公路暂时封闭；/n4. 车辆减速慢行，能见度很差时应停靠在路边安全地带；/n5. 紧闭或密封门窗，不要外出；对老人、儿童和心血管病人、呼吸道病人实施特别护理。必须出行时，用纱巾、风镜和口罩保护眼、鼻、口，要注意交通和人身安全； /n6. 出行归来后，应尽快漱口刷牙，用清水洗眼，用蘸酒精的棉签洗耳，用浓度约0.9%的盐水冲洗鼻孔，将鼻、嘴、眼、耳中的各类有害物质清洗干净。",
			"标准：6小时可能出现强沙尘暴天气，能见度小于500米；或者已经出现强沙尘暴天气并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责启动防沙尘暴应急工作，交通、卫生等部门与单位应立即采取措施，保障交通和卫生安全，民航机场和高速公路应根据能见度变化，适时关闭，有关部门和单位注意关注森林、草场和城区防火工作；/n2. 停止露天集会、体育活动和高空、水上等户外生产作业和游乐活动；/n3. 立即关闭门窗，必要时可用胶条对门窗进行密封；加固易被风吹动的搭建物，安置和遮盖好易受大风影响的室外物品，密封好精密仪器；/n4. 驾驶人员要密切关注路况，谨慎驾驶，减速慢行；/n5. 避免外出，加强对老人、儿童及患有呼吸道疾病的人的护理；户外人员应当带好口罩、纱巾等防沙尘用品；外出归来，应尽快清洗鼻、嘴、眼、耳中的沙尘及有害物质。",
			"标准： 12小时可能出现扬沙或浮尘天气，或者已经出现扬沙或浮尘天气并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防沙尘工作；/n2. 停止露天集会和室外体育活动；/n3. 关好门窗，加固围板、棚架、广告牌等易被风吹动的搭建物，妥善安置易受大风影响的室外物品，遮盖建筑物资； /n4. 尽量减少外出，老人、儿童及患有呼吸道过敏性疾病的人群不要到室外活动；人员外出时可佩戴口罩、纱巾等防尘用品，外出归来应清洗面部和鼻腔。",
			"标准：24小时最低气温将要下降16℃以上，最低气温小于等于0℃，陆地平均风力可达6级以上；或者已经下降16℃以上，最低气温小于等于0℃，平均风力达6级以上，并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防寒潮的应急和抢险工作，加强交通安全、防风、防火工作，避免火借风势，造成重大损失与伤亡；/n2. 农、林、养殖业积极采取防霜冻、冰冻等防寒措施，全面强化对作物、树木、牲畜以及大棚、温室的防冻害管理；/n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒； /n4. 大风天气应及时加固围板、棚架、广告牌等易被大风吹动的搭建物，停止高空作业及室外高空游乐项目。/n5. 幼儿园、中小学应采取防风防寒措施；老弱病幼人群切勿在大风天外出，特别注意对心血管病人、哮喘病人加强护理；/n6. 个人要采取防寒防风措施，严防感冒和冻伤；外出时远离施工工地；驾驶人员应注意路況，慢速行驶，不在高大建筑物、广告牌与大树下方停留或停车。",
			"标准：24小时最低气温将要下降12℃以上，最低气温小于等于0℃，陆地平均风力可达6级以上；或者已经下降12℃以上，最低气温小于等于0℃，平均风力达6级以上，并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防寒潮的应急工作，密切关注火灾隐患，防止发生火灾事故；/n2. 农、林、养殖业注意防范有可能发生的冰冻现象，强化对大棚、温室的管理，对作物、树木、牲畜等采取有效的防冻措施；/n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 大风天气应及时加固围板、棚架、广告牌等易被大风吹动的搭建物，停止高空作业及室外高空游乐项目；/n5. 老弱病人，特别是心血管病人、哮喘病人等对气温变化敏感的人群避免外出/n6. 个人外出应采取防寒防风措施，远离施工工地；驾驶人员应注意路況，慢速行驶，不在高大建筑物、广告牌与大树下方停留或停车。",
			"标准：24小时最低气温将要下降10℃以上，最低气温小于等于4℃，陆地平均风力可达6级以上；或者已经下降10℃以上，最低气温小于等于4℃，平均风力达6级以上，并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防寒潮工作，增强防火安全意识；/n2. 农、林、养殖业做好作物、树木与牲畜防冻害工作；设施农业生产企业和农户加强温室内温度调控，防止经济植物遭受冻害； /n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 大风天气应及时加固围板、棚架、广告牌等易被大风吹动的搭建物，妥善安置易受大风影响的室外物品；停止高空作业及室外高空游乐项目；/n5. 老、弱、病、幼，特别是心血管病人、哮喘病人等对气温变化敏感的人群尽量不要外出；/n6. 个人外出注意防寒，尽量远离施工工地，不应在高大建筑物、广告牌或大树下方停留。",
			"标准：48小时最低气温将要下降8℃以上，最低气温小于等于4℃，陆地平均风力可达5级以上；或者已经下降8℃以上，最低气温小于等于4℃，平均风力达5级以上，并可能持续。/n防御指南： /n1．政府及有关部门按照职责做好防寒潮准备工作； /n2．农、林、养殖业做好作物、树木与牲畜防冻害准备；设施农业生产企业和农户注意温室内温度的调控，防止蔬菜和花卉等经济植物遭受冻害；/n3．有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 注意防风，关好门窗，加固室外搭建物。/n5. 老弱病人，特别是心血管病人、哮喘病人等对气温变化敏感的人群应减少外出；/n6. 个人应注意添衣保暖，做好对大风降温天气的防御准备出行时，注意带上帽子、围巾和手套。",
			"标准：6小时降雪量将达15毫米以上，或者已达15毫米且降雪可能持续。/n防御指南：/n1.地方各级人民政府、有关部门和单位按照职责做好防雪灾和防冻害的应急和抢险工作，职能部门及其公共服务、事业单位全面启动减灾抗灾救灾工作预案；/n2. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n3. 必要时停课、停业（除特殊行业外）、停止集会，飞机暂停起降，火车暂停运行，高速公路暂时封闭；/n4. 车辆尽量不出行，必须出行时应减速慢行，避免急刹车；雪地行车时可给轮胎适当放气或安装防滑链。/n5. 人员尽量不外出，必须外出时应步行或乘公共交通工具，老、弱、病、幼人群不要外出；野外出行应戴防护眼镜；被风雪围困时应及时拨打求救电话；/n6. 危旧房屋内的人员要迅速撤出；行人要远离大树、广告牌和临时搭建物，避免砸伤；路过桥下、屋檐等处时，要小心观察或绕道通过，以免因冰凌融化脱落伤人。",
			"标准：6小时降雪量将达10毫米以上，或者已达10毫米且降雪可能持续。/n防御指南：/n1.地方各级人民政府、有关部门和单位按照职责做好防雪灾和防冻害的应急工作，交通、电力、通信、市政等部门随时进行道路、铁路、线路巡查维护，随时清扫道路和融化积雪，做好生活必需品调度供应工作；/n2. 农、林、养殖业做好冻害与雪灾的防御、减缓与救援；及时加固各类易被大雪压垮的大棚、树木、设施与建筑物等，及时清除棚顶及树上积雪； /n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 必要时中小学、幼儿园可错峰上下学，企事业单位错峰上下班；/n5. 车辆不建议出行，必须外出时可给轮胎适当放气，注意防滑，遇坡道或转弯时提前减速，缓慢通过，慎用刹车装置；/n6. 人员外出最好选择步行或乘公共交通工具；行走时应避开广告牌、临时搭建物和大树；老、弱、病、幼人群不宜外出；野外出行应戴黑色太阳镜；/n7. 不要待在危房以及结构不安全的房子中，避免屋塌伤人；雪后化冻时房檐如果结有长而大的冰柱应及早提前打掉，以免坠落砸人。",
			"标准：12小时降雪量将达6毫米以上，或者已达6毫米且降雪可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责落实防雪灾和防冻害措施，交通、电力、通信、市政等部门及时进行道路、铁路、线路巡查维护，及时清扫道路和融化积雪；/n2. 农、林、养殖业应做好作物、树木防冻害、牲畜防寒与防雪灾工作；对危房、大棚和临时搭建物及大树、古树采取加固措施，及时清除棚顶及树上积雪；/n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 车辆减少出行，外出时可给轮胎适当放气，注意路况、保持车距、减速慢行；/n5. 人员外出要少骑或不骑自行车，出行不穿硬底、光滑底的鞋；老、弱、病、幼减少出行，外出时必须有人陪护；/n6. 不要待在危房中，避免屋塌伤人。",
			"标准：12小时降雪量将达4毫米以上，或者已达4毫米且降雪可能持续，对交通及农业可能有影响。/n防御指南： /n1. 政府及有关部门按照职责做好防雪灾和防冻害准备工作，交通、电力、通信、市政等部门应当进行道路、线路巡查维护，做好道路清扫和积雪融化工作；/n2. 农、林、养殖业应做好作物、树木防冻害与牲畜防寒准备；对危房、大棚和临时搭建物采取加固措施，及时清除积雪；/n3. 有关部门视情况调节居民供暖，燃煤取暖用户注意防范一氧化碳中毒；/n4. 车辆尽量减少出行，外出应注意路况，听从指挥，慢速驾驶； /n5. 人员外出应少骑自行车，采取保暖防滑措施；老、弱、病、幼尽量减少出行，外出应有人陪护。",
			"标准：6小时内可能或者已经受热带气旋影响，平均风力达12级以上，或者阵风达14级以上并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防台风应急和抢险工作，立即转移危险地带人员及灾民，立即做好排涝、排水抢险，并随时启动台风引发各种次生灾害（停电、燃气泄露、火灾等）的应急救援工作；/n2. 飞机暂停起降，火车暂停运行，高速公路暂时封闭；暂时关闭景区；做好养殖业、农业防灾工作；/n3. 立即停止集会、停课、停业（除特殊行业外），船只立即停驶；/n4. 紧闭室内每个门窗，立即用胶条对门窗进行密封，并在窗玻璃上贴上米字形胶条。彻查、消除室内电路、炉火等设施隐患，关闭煤气阀，确保房屋及建筑物安全；/n5. 人员车辆禁止出行；/n6、驾驶人员在途中突遇台凤必须立刻靠边停车或迅速将车开到最近的安全区域；/n7. 行人如遇到台风加上打雷，要采取防雷措施，以最快速度找安全处躲避，避免在广告牌、铁塔、电线杆、大树下或其附近停留；/n8. 台风眼经过时，强风暴雨会突然转为风停雨止的短时平静状况，不要急于外出，应在安全处多待1--2小时，待确认台风完全过境后再外出；台风过后，应注意卫生和食品、水的安全。",
			"标准：12小时内可能或者已经受热带气旋影响,平均风力达10级以上，或者阵风12级以上并可能持续。 /n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防台风抢险应急工作，立即转移住在危房及低洼地区人员，启动排涝、排水应急工作，加强城市供电线路巡查、监测工作，及时做好防范台风引发的次生灾害；/n2. 实施交通管制，园林、建筑部门与有关单位立即强化管理和实施防台风行动，旅游部门立即并持续发布不去台风经过区域旅游的警告；/n3. 停止室内外大型集会和户外作业，立即将人员转移到安全地带；幼儿园和学校停课；中心商业区及时加强防雨、防风措施，并关门停业；/n4. 紧闭室内门窗，及时在窗玻璃上贴上米字形胶条并远离窗口，以免强风席卷散物击破玻璃伤人；排查和清除室内电路、炉火、煤气阀等设施隐患，保障安全；/n5. 人员车辆避免出行；/n6、驾驶人员在途中突遇台凤要密切关注路况，听从指挥，慢速驾驶，立即将车开到安全区域或附近的地下停车场；/n7、行人立即到安全地带躲避，避免在广告牌、铁塔、大树下或近旁停留；立即停止一切室外水上活动.",
			"标准：24小时内可能或者已经受热带气旋影响,平均风力达8级以上，或者阵风10级以上并可能持续。/n防御指南：/n1. 地方各级人民政府、有关部门和单位按照职责做好防台风应急准备工作，及时转移住在危房及低洼地区人员，做好排涝、清理排水管道以及防大风暴雨、地质灾害的工作；/n2.采取交通管控措施，立即加固门窗、围板、棚架、广告牌等易被风吹动的搭建物,切断危险的室外电源；/n3. 停止露天集体活动、高空等户外危险作业和室内大型集会，并做好人员转移工作；幼儿园和中小学必要时可停课；/n4. 室内关闭门窗，在窗玻璃上用胶条贴成“米”字图形，并立即收取室外与阳台上的物品；检查电路、炉火、煤气阀等设施，以保安全; /n5.机动车驾驶员要关注路况，听从指挥，避开道路积水和交通阻塞区段，或及时将车开到安全处或地下停车场; /n6. 人员尽量避免出行；/n7. 行人立即到室内躲避，避免在广告牌、铁塔、大树下或近旁停留；停止一切室外水上活动。",
			"标准: 24小时内可能或者已经受热带气旋影响,平均风力达6级以上，或者阵风8级以上并可能持续。/n防御指南：/n1. 政府及相关部按照职责做好防台风准备工作，转移住在危房及低洼地区人员，清理排水管道和做好排涝准备，注意防范大风和泥石流等灾害；/n2. 采取交通管控措施，加固门窗、围板、棚架、广告牌等易被风吹动的搭建物,切断危险的室外电源；/n3. 停止露天集体活动和高空等户外危险作业；幼儿园和中小学采取暂避措施或视情况提前或推迟上下学时间；/n4. 关好门窗，提前收取露台、阳台上的花盆、晾晒物品等，检查电路、炉火、煤气阀等设施是否安全；/n5. 人员不宜出行，出行时避免使用自行车等人力交通工具。遇到大风大雨，应立即到室内躲避，不要在广告牌、铁塔、大树下或近旁停留；/n6. 注意台风预报，不去台风可能经过的地区旅游。台风影响期间避免各类室外水上活动。"

	};

	private int[] yujingImageArray = { 
	R.drawable.alert14_0,

	R.drawable.alert14_1,

	R.drawable.alert14_2,

	R.drawable.alert14_3, R.drawable.alert7_0,

	R.drawable.alert7_1,

	R.drawable.alert7_2, R.drawable.alert9_0,

	R.drawable.alert9_1,

	R.drawable.alert9_2,

	R.drawable.alert9_3, R.drawable.alert11_0,

	R.drawable.alert11_1,

	R.drawable.alert11_2,

	R.drawable.alert11_3, R.drawable.alert3_0,

	R.drawable.alert3_1,

	R.drawable.alert3_2, R.drawable.alert0_2,

	R.drawable.alert0_3,

	R.drawable.alert1_1,

	R.drawable.alert1_2,

	R.drawable.alert2_0,

	R.drawable.alert2_1,

	R.drawable.alert2_2,

	R.drawable.alert4_0,

	R.drawable.alert4_1,

	R.drawable.alert4_2,

	R.drawable.alert5_1,

	R.drawable.alert5_2,

	R.drawable.alert5_3,

	R.drawable.alert6_0,

	R.drawable.alert6_1,

	R.drawable.alert6_2,

	R.drawable.alert8_0,

	R.drawable.alert8_1,

	R.drawable.alert10_0,

	R.drawable.alert10_1,

	R.drawable.alert10_2,

	R.drawable.alert12_0,

	R.drawable.alert12_1,

	R.drawable.alert12_2,

	R.drawable.alert12_3,

	R.drawable.alert13_0,

	R.drawable.alert13_1,

	R.drawable.alert13_2,

	R.drawable.alert13_3,

	R.drawable.alert15_0, R.drawable.alert15_1, R.drawable.alert15_2,
			R.drawable.alert15_3 };
	private final int REFRESH_LISTVIEW = 1;
	private final int NO_MESSAGE = 2;
	private boolean showYuJingList = true;
	private ProgressDialog progressdialog;
	private boolean isComefromMessageInfo = false;
	private final String COME_FROM_MESSAGESINFO = "com.zhkt.comefrommessageinfo";

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {

			case REFRESH_LISTVIEW:
				messageAdapter.notifyDataSetChanged();
				serch_message_list.setVisibility(View.VISIBLE);
				break;
			case NO_MESSAGE:
				ShowToast("没有相关通知");
				break;
			default:
				break;
			}
		}

	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabsearchactive3);
		yujingArrayList.add("预警信号");

		quiteAlertDialog = new AlertDialog.Builder(this).setTitle("退出应用?")
				.setMessage("点击确定按钮退出北京服务您")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							SysUtils.log("当前需要销毁activity数量 = "
									+ AppContex.activities.size());
							for (Activity activity : AppContex.activities) {
								activity.finish();
							}
							AppContex.APPCATION_ON = false;
							AppContex.tempInfos.clear();
							finish();
						} catch (Exception e) {
							e.printStackTrace();
						}

						onBackPressed();
					}
				}).setNegativeButton("取消", null).create();

		search_bar = (EditText) findViewById(R.id.search_view);

		serch_Commitbutton = (TextView) findViewById(R.id.serch_commitbutton);
		serch_Commitbutton.setOnClickListener(this);
		yujingtypelistview = (ExpandableListView) findViewById(R.id.yujingtypelistview);
		yujingtypelistview.setAdapter(new MyAdapter());
		yujingtypelistview.setOnItemClickListener(this);
		serch_message_list = (ListView) findViewById(R.id.message_listivew);
		messageAdapter = new MessageAdapter();
		serch_message_list.setAdapter(messageAdapter);
		serch_message_list.setOnItemClickListener(this);
		alertImage = (ImageView) findViewById(R.id.alertimage);
		progressdialog = new ProgressDialog(this);
		progressdialog.setMessage("正在查询请稍后");
		this.registerReceiver(comefrommessageinfoBroadcastReceiver,
				new IntentFilter(COME_FROM_MESSAGESINFO));

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int type;
		keyWordsString = search_bar.getEditableText().toString().trim();

		switch (v.getId()) {
		//单击查询按钮
		case R.id.serch_commitbutton:
			// alertlayout.setVisibility(View.INVISIBLE);
			yujingtypelistview.setVisibility(View.INVISIBLE);
			if (SysUtils.isStringEmpty(keyWordsString)) {
				ShowToast("请你输入查询内容");
				break;
			}
			searchedInfos.clear();

			if (SysUtils.checkNetworkConnectedStat(TabSercherActive2.this)) {
				progressdialog.show();
				searchThread = new SearchThread(0, keyWordsString);
				searchThread.start();
			} else {
				Toast.makeText(TabSercherActive2.this, "网络未连接",
						Toast.LENGTH_SHORT).show();
			}

			break;

		case R.id.alertlayout:
			showYuJingList = !showYuJingList;
			serch_message_list.setVisibility(View.INVISIBLE);
			if (showYuJingList) {
				alertImage.setBackgroundResource(R.drawable.right);
				yujingtypelistview.setVisibility(View.VISIBLE);
			} else {
				alertImage.setBackgroundResource(R.drawable.down);
				yujingtypelistview.setVisibility(View.INVISIBLE);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {

		quiteAlertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "退出");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		quiteAlertDialog.show();
		return true;
	}
	//启动查询请求线程
	private class SearchThread extends Thread {

		int type;
		String keyWordsString;

		public SearchThread(int type, String keyWordsString) {
			// TODO Auto-generated constructor stub
			this.type = type;
			this.keyWordsString = keyWordsString;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			String Url = AppContex.SEND_SERCH_API + "deviceid="
					+ Uri.encode(SysUtils.getDeviceID(TabSercherActive2.this))
					+ "&type=" + type + "&keywords=" + keyWordsString
					+ "&client=android";
			HttpGet httpGet = new HttpGet(Url);
			SysUtils.log("SEARCHAPI:   " + Url);

			try {
				HttpResponse httpResponse = new DefaultHttpClient()
						.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String resultString = EntityUtils.toString(httpResponse
							.getEntity());
					System.out.println("SEARCHAPIRESULT:     " + resultString);
					JSONArray jsonArray = new JSONArray(resultString);
					if (jsonArray.length() == 0) {
						Message msgMessage1 = new Message();
						msgMessage1.what = NO_MESSAGE;
						handler.sendMessage(msgMessage1);
						progressdialog.dismiss();
						return;
					}

					searchedInfos.clear();
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						if (type == 2) {
							int type = jsonObject.getInt("type");
							int level = jsonObject.getInt("level");
							System.out.println("type:level" + type + ":"
									+ level);
							start_alerContent(type, level);

						}

						if (type == 0) {
							MessageInfo messageInfo = new MessageInfo();
							messageInfo.content = jsonObject
									.getString("content");
							messageInfo.day = jsonObject.getInt("day");
							messageInfo.fcatid = jsonObject.getInt("fcatid");
							messageInfo.hour = jsonObject.getInt("hour");
							messageInfo.min = jsonObject.getInt("min");
							messageInfo.month = jsonObject.getInt("month");
							messageInfo.readState = MessageInfo.READ_STATE_UNREAD;
							messageInfo.scatid = jsonObject.getInt("scatid");
							messageInfo.serversqlid = jsonObject.getInt("id");
							messageInfo.title = jsonObject.getString("title");
							messageInfo.year = jsonObject.getInt("year");
							messageInfo.type = jsonObject.getInt("type");

							if (jsonObject.has("alertlevel")
									&& !jsonObject.isNull("alertlevel")) {
								messageInfo.alertlevel = jsonObject
										.getInt("alertlevel");
							} else {
								messageInfo.alertlevel = -1;
							}

							if (jsonObject.has("alerttype")
									&& !jsonObject.isNull("alerttype")) {
								messageInfo.alerttype = jsonObject
										.getInt("alerttype");
							} else {
								messageInfo.alerttype = -1;
							}

							searchedInfos.add(messageInfo);

						}
					}

					if (messageAdapter != null) {
						Message msgMessage2 = new Message();
						msgMessage2.what = REFRESH_LISTVIEW;
						handler.sendMessage(msgMessage2);

					}

				} else {
					Message msg = new Message();
					msg.what = NO_MESSAGE;
					handler.sendMessage(msg);
				}

			} catch (ClientProtocolException qwertyuiopasdfghjklzxcvbnm) {
				// TODO Auto-generated catch block
				qwertyuiopasdfghjklzxcvbnm.printStackTrace();
			} catch (IOException qwertyuiopasdfghjklzxcvbnm) {
				// TODO Auto-generated catch block
				qwertyuiopasdfghjklzxcvbnm.printStackTrace();
			} catch (JSONException qwertyuiopasdfghjklzxcvbnm) {
				// TODO Auto-generated catch block
				qwertyuiopasdfghjklzxcvbnm.printStackTrace();
			}
			progressdialog.dismiss();
		}

	}

	private void ShowToast(String toastStr) {
		Toast.makeText(TabSercherActive2.this, toastStr, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		search_bar.setText("");
		//重置listview 界面
		yujingtypelistview.setVisibility(View.VISIBLE);
		if (isComefromMessageInfo) {
			serch_message_list.setVisibility(View.VISIBLE);
			yujingtypelistview.setVisibility(View.INVISIBLE);
			isComefromMessageInfo = false;
		} else {
			serch_message_list.setVisibility(View.INVISIBLE);
			yujingtypelistview.setVisibility(View.VISIBLE);
		}

		super.onResume();
	}

	private class MyAdapter extends BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return yujingSubArrayStrings[childPosition];
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(TabSercherActive2.this)
						.inflate(R.layout.yujing_list_title2, null);
			}
			RelativeLayout yujingLinearLayout = (RelativeLayout) convertView
					.findViewById(R.id.yudingtypelayout);
			yujingLinearLayout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent();
					intent.putExtra("alerttitle",
							yujingSubArrayStrings[childPosition]);
					intent.putExtra("alertimage",
							yujingImageArray[childPosition]);
					intent.putExtra("alertcontent",
							yujingSubContentArray[childPosition]);
					intent.setClass(TabSercherActive2.this,
							AlertContentActivity2.class);
					TabSercherActive2.this.startActivity(intent);
				}
			});
			TextView typetitle = (TextView) convertView
					.findViewById(R.id.yujingtype_subtitle);
			typetitle.setText(yujingSubArrayStrings[childPosition]);

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return yujingSubArrayStrings.length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return yujingArrayList.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return yujingArrayList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(TabSercherActive2.this)
						.inflate(R.layout.yujing_list_title, null);
			}
			TextView typetitle = (TextView) convertView
					.findViewById(R.id.yujingtype_title);
			typetitle.setText(yujingArrayList.get(groupPosition));
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

	}
	//发送预警图片id
	public int sendIntentResouce(int messagesAlertType, int messagesAlertLevel) {
		// TODO Auto-generated method stub

		if (messagesAlertType == 0 && messagesAlertLevel == 2) {
			return R.drawable.alert0_2;
		}

		if (messagesAlertType == 0 && messagesAlertLevel == 3) {
			return R.drawable.alert0_3;
		}

		if (messagesAlertType == 1 && messagesAlertLevel == 1) {
			return R.drawable.alert1_1;
		}

		if (messagesAlertType == 1 && messagesAlertLevel == 2) {
			return R.drawable.alert1_2;
		}

		if (messagesAlertType == 10 && messagesAlertLevel == 0) {
			return R.drawable.alert10_0;
		}

		if (messagesAlertType == 10 && messagesAlertLevel == 1) {
			return R.drawable.alert10_1;
		}

		if (messagesAlertType == 10 && messagesAlertLevel == 2) {
			return R.drawable.alert10_2;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 0) {
			return R.drawable.alert11_0;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 1) {
			return R.drawable.alert11_1;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 2) {
			return R.drawable.alert11_2;
		}

		if (messagesAlertType == 11 && messagesAlertLevel == 3) {
			return R.drawable.alert11_3;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 0) {
			return R.drawable.alert12_0;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 1) {
			return R.drawable.alert12_1;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 2) {
			return R.drawable.alert12_2;
		}

		if (messagesAlertType == 12 && messagesAlertLevel == 3) {
			return R.drawable.alert12_3;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 0) {
			return R.drawable.alert13_0;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 1) {
			return R.drawable.alert13_1;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 2) {
			return R.drawable.alert13_2;
		}

		if (messagesAlertType == 13 && messagesAlertLevel == 3) {
			return R.drawable.alert13_3;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 0) {
			return R.drawable.alert14_0;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 1) {
			return R.drawable.alert14_1;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 2) {
			return R.drawable.alert14_2;
		}

		if (messagesAlertType == 14 && messagesAlertLevel == 3) {
			return R.drawable.alert14_3;
		}

		if (messagesAlertType == 2 && messagesAlertLevel == 0) {
			return R.drawable.alert2_0;
		}

		if (messagesAlertType == 2 && messagesAlertLevel == 1) {
			return R.drawable.alert2_1;
		}

		if (messagesAlertType == 2 && messagesAlertLevel == 2) {
			return R.drawable.alert2_2;
		}

		if (messagesAlertType == 3 && messagesAlertLevel == 1) {
			return R.drawable.alert3_1;
		}

		if (messagesAlertType == 3 && messagesAlertLevel == 2) {
			return R.drawable.alert3_2;
		}

		if (messagesAlertType == 4 && messagesAlertLevel == 0) {
			return R.drawable.alert4_0;
		}

		if (messagesAlertType == 4 && messagesAlertLevel == 1) {
			return R.drawable.alert4_1;
		}

		if (messagesAlertType == 4 && messagesAlertLevel == 2) {
			return R.drawable.alert4_2;
		}

		if (messagesAlertType == 5 && messagesAlertLevel == 1) {
			return R.drawable.alert5_1;
		}

		if (messagesAlertType == 5 && messagesAlertLevel == 2) {
			return R.drawable.alert5_2;
		}

		if (messagesAlertType == 5 && messagesAlertLevel == 3) {
			return R.drawable.alert5_3;
		}

		if (messagesAlertType == 6 && messagesAlertLevel == 0) {
			return R.drawable.alert6_0;
		}

		if (messagesAlertType == 6 && messagesAlertLevel == 1) {
			return R.drawable.alert6_1;
		}

		if (messagesAlertType == 6 && messagesAlertLevel == 2) {
			return R.drawable.alert6_2;
		}

		if (messagesAlertType == 7 && messagesAlertLevel == 0) {
			return R.drawable.alert7_0;
		}

		if (messagesAlertType == 7 && messagesAlertLevel == 1) {
			return R.drawable.alert7_1;
		}

		if (messagesAlertType == 7 && messagesAlertLevel == 2) {
			return R.drawable.alert7_2;
		}

		if (messagesAlertType == 8 && messagesAlertLevel == 0) {
			return R.drawable.alert8_0;
		}

		if (messagesAlertType == 8 && messagesAlertLevel == 1) {
			return R.drawable.alert8_1;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 0) {
			return R.drawable.alert9_0;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 1) {
			return R.drawable.alert9_1;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 2) {
			return R.drawable.alert9_2;
		}

		if (messagesAlertType == 9 && messagesAlertLevel == 3) {
			return R.drawable.alert9_3;
		}
		return -1;
	}


    //进入预警信息详情
	private void start_alerContent(int alerttype, int alertlevel) {
		Intent intent = new Intent(TabSercherActive2.this,
				AlertContentActivity.class);
		Bundle extras = new Bundle();
		extras.putInt("ALERTTYPE", alerttype);
		extras.putInt("ALERTLEVEL", alertlevel);
		int sendResouceInt = sendIntentResouce(alerttype, alertlevel);
		if (sendResouceInt != -1) {
			extras.putInt("ALERTRESOURCEINT", sendResouceInt);
			intent.putExtras(extras);
			TabSercherActive2.this.startActivity(intent);
		}
	}

	private class MessageAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub

			return searchedInfos.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return searchedInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(TabSercherActive2.this)
						.inflate(R.layout.tabmessage_list_single, null);
			}

			final MessageInfo messageInfo = searchedInfos.elementAt(position);
			System.out.println("TITLE:   " + messageInfo.title);
			// System.out.println("ID  "+messageInfo.);
			System.out.println("TYPE:   " + messageInfo.type);
			TextView titleTextView = (TextView) convertView
					.findViewById(R.id.tabmessage_list_single_title_textview);
			TextView timeTextView = (TextView) convertView
					.findViewById(R.id.tabmessage_list_single_time_textview);
			TextView contentTextView = (TextView) convertView
					.findViewById(R.id.tabmessage_list_single_content);
			TextView typeinfo = (TextView) convertView
					.findViewById(R.id.tabmessage_list_infotype);
			RelativeLayout typeimagelayout = (RelativeLayout) convertView
					.findViewById(R.id.typeimagelayout);
			typeinfo.setText("");
			contentTextView.setTextColor(TabSercherActive2.this.getResources()
					.getColor(R.color.dark));
			titleTextView.setText(messageInfo.title == null ? ""
					: messageInfo.title);
			timeTextView
					.setText(messageInfo.getFormatedTimeWithoutYear() == null ? ""
							: messageInfo.getFormatedTimeWithoutYear());
			contentTextView.setText(messageInfo.content == null ? ""
					: messageInfo.content);

			TextView cattitleTextView = (TextView) convertView
					.findViewById(R.id.tabmessage_list_single_cattitle_textview);
			cattitleTextView.setText("");
			int messagesAlertType = messageInfo.alerttype;
			int messagesAlertLevel = messageInfo.alertlevel;
			ImageView altertTypeView = (ImageView) convertView
					.findViewById(R.id.typeimage);

			if (messagesAlertType != -1 && messagesAlertLevel != -1) {
				typeimagelayout.setVisibility(View.VISIBLE);
				altertTypeView.setVisibility(View.VISIBLE);
				setAlertTypeImage(messagesAlertType, messagesAlertLevel,
						altertTypeView);

				altertTypeView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						System.out.println("ALERTTYPE  GET:ALERTLEVEL SEND:  "
								+ messageInfo.alerttype + ":  "
								+ messageInfo.alertlevel);

						Bundle extras = new Bundle();
						extras.putInt("ALERTTYPE", messageInfo.alerttype);
						extras.putInt("ALERTLEVEL", messageInfo.alertlevel);
						int sendResouceInt = sendIntentResouce(
								messageInfo.alerttype, messageInfo.alertlevel);
						if (sendResouceInt != -1) {
							extras.putInt(
									"ALERTRESOURCEINT",
									sendIntentResouce(messageInfo.alerttype,
											messageInfo.alertlevel));
							Intent intent = new Intent(TabSercherActive2.this,
									AlertContentActivity.class);
							intent.putExtras(extras);
							TabSercherActive2.this.startActivity(intent);
						} else {
							Toast.makeText(TabSercherActive2.this, "网络忙，请稍后",
									Toast.LENGTH_SHORT);
						}

					}
				});
			} else {
				typeimagelayout.setVisibility(View.GONE);
				titleTextView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
				contentTextView.getLayoutParams().width = RelativeLayout.LayoutParams.MATCH_PARENT;
			}

			try {
				for (FirstCatalogue firstCatalogue : AppContex.catalogues) {
					if (firstCatalogue.fcatid == messageInfo.fcatid) {
						String titleString = firstCatalogue.title.replace("北京",
								"");
						cattitleTextView.setText(titleString);
					}

					for (SecondCatalogue secondCatalogue : firstCatalogue.secondCatalogues) {
						if (secondCatalogue.scatid == messageInfo.scatid) {
							typeinfo.setText(secondCatalogue.title);
						}
					}
				}
			} catch (Exception e) {

			}

			if (messageInfo.readState == MessageInfo.READ_STATE_READED) {

				titleTextView.setTextColor(0xff333333);
				titleTextView.setTextAppearance(TabSercherActive2.this,
						R.style.text_normal);
			} else {

				titleTextView.setTextColor(0xff222222);
				titleTextView.setTextAppearance(TabSercherActive2.this,
						R.style.text_bold);
			}
			if (messageInfo.type == MessageInfo.MESSAGE_TYPE_ERGENT) {
				titleTextView.setTextColor(TabSercherActive2.this
						.getResources().getColor(R.color.red));
				typeinfo.setTextColor(TabSercherActive2.this.getResources()
						.getColor(R.color.red));
			} else {
				typeinfo.setTextColor(TabSercherActive2.this.getResources()
						.getColor(R.color.bule));
			}

			return convertView;
		}

	}
	//显示预警图片
	public void setAlertTypeImage(int messagesAlertType,
			int messagesAlertLevel, ImageView alertImage) {
		// TODO Auto-generated method stub
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		int sampleSize = 8;
		options.inJustDecodeBounds = false;
		options.inSampleSize = sampleSize;

		try {

			if (messagesAlertType == 0 && messagesAlertLevel == 2) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert0_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 0 && messagesAlertLevel == 3) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert0_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 1 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert1_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 1 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert1_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert10_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert10_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 10 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert10_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert11_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 1) {
				alertImage.setBackgroundResource(R.drawable.alert11_1);
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert11_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 2) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert11_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 11 && messagesAlertLevel == 3) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert11_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert12_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert12_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 2) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert12_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 12 && messagesAlertLevel == 3) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert12_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert13_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert13_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert13_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 13 && messagesAlertLevel == 3) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert13_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert14_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 1) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert14_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert14_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 14 && messagesAlertLevel == 3) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert14_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert15_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 1) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert15_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert15_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 15 && messagesAlertLevel == 3) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert15_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert2_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert2_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 2 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert2_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert3_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert3_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 3 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert3_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert4_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert4_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 4 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert4_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert5_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert5_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 5 && messagesAlertLevel == 3) {
				alertImage.setBackgroundResource(R.drawable.alert5_3);
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert5_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert6_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert6_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 6 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert6_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert7_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert7_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 7 && messagesAlertLevel == 2) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert7_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 8 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert8_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 8 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert8_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 0) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert9_0, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 1) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert9_1, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 2) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert9_2, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}

			if (messagesAlertType == 9 && messagesAlertLevel == 3) {

				Bitmap bitmap = BitmapFactory.decodeResource(
						TabSercherActive2.this.getResources(),
						R.drawable.alert9_3, options);
				alertImage.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			alertImage.setVisibility(View.VISIBLE);

		} catch (Exception e) {
			e.printStackTrace();
			alertImage.setVisibility(View.GONE);
		}

	}
	//进入通知详情
	@Override
	public void onItemClick(AdapterView<?> adapterView, View arg1,
			int position, long arg3) {
		// TODO Auto-generated method stub
		if (adapterView == serch_message_list) {

			AppContex.curMessageInfo = searchedInfos.elementAt(position);
			Intent intent = new Intent(TabSercherActive2.this,
					MessageInfoDetailActivity.class);
			Bundle extras = new Bundle();
			extras.putBoolean("ISFAVORATEACTTIVITY", true);
			extras.putBoolean("ISTABSEARCHACTTIVITY", true);
			extras.putBoolean("ISCOMEFROMNOTIFY", false);
			extras.putInt("MESSAGES_ID", -1);
			intent.putExtras(extras);
			startActivity(intent);

		}

	}

	private BroadcastReceiver comefrommessageinfoBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(COME_FROM_MESSAGESINFO)) {
				isComefromMessageInfo = true;
			}
		}

	};
}
