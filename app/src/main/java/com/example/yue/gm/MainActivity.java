package com.example.yue.gm;

import android.app.Activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import data.Const;
import model.IWordButtonClickListener;
import model.Song;
import model.WordButton;
import myui.MyGridView;
import util.Util;

import static data.Const.INDEX_FILE_NAME;
import static data.Const.INDEX_SONG_NAME;

public class MainActivity extends Activity  implements IWordButtonClickListener{

    //唱片动画定义
    private Animation mPanAnim;//定义动画
    private LinearInterpolator mPanLin;//定义动画线性速度
    //拨杆进唱片动画定义
    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;
    //拨杆弹出动画定义
    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;

    //播放按钮事件定义
    private ImageButton mBtnPlaystart;

    //设置动画是否在播放判断
    private boolean mIsRuning = false;

    //定义唱片和拨杆的控件
    private ImageView mViewPan;
    private ImageView mViewPanbar;

    //文字框容器
    private ArrayList<WordButton> mAllWords;

    //已选框文字容器
    private ArrayList<WordButton> mBtnSelectWords;

    //待选文本框
    private MyGridView mMygridView;

    //已选文本框
    private LinearLayout mViewWordsCantainer;

    //当前歌曲的对象
    private Song mCurrentSong;

    //当前关的索引,因为数组的索引问题，所以初始化为-1
    private int mCurrentStageIndex = -1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnPlaystart = (ImageButton)findViewById(R.id.btn_play_start);
        mViewPan = (ImageView)findViewById(R.id.pan_1);
        mViewPanbar = (ImageView)findViewById(R.id.pin_1);
        mMygridView = (MyGridView)findViewById(R.id.gridview);//待选文本框
        mViewWordsCantainer = (LinearLayout)findViewById(R.id.word_select_container);//已选文本框
        //注册监听
        mMygridView.registOnWordButtonClick(this);


        //初始化唱片动画
        mPanAnim = AnimationUtils.loadAnimation(this,R.anim.rotate);
        mPanLin = new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);//设置线性速度
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            //设置动画监听器
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPanbar.startAnimation(mBarOutAnim);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //动画重复

            }
        });



        //初始化拨杆In动画
        mBarInAnim = AnimationUtils.loadAnimation(this,R.anim.rotate_45);
        mBarInLin = new LinearInterpolator();
        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //初始化拨杆弹出动画
        mBarOutAnim = AnimationUtils.loadAnimation(this,R.anim.rotate_d_45);
        mBarOutLin = new LinearInterpolator();
        mBarOutAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsRuning = false;//播放完动画后，设置mIsRuning为false，之后点击播放按钮就可以执行动画
                mBtnPlaystart.setVisibility(View.VISIBLE);//播放完毕设置播放按钮可见
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        mBtnPlaystart.setOnClickListener(new View.OnClickListener() {
            //设置播放按钮点击事件
            @Override
            public void onClick(View v) {
                hangdlePlayButton();
            }
        });

        //获取更新文本框数据
        initCurrentStageData();
    }

    @Override
    public void onWordButtonClick(WordButton wordButton){
        Toast.makeText(this,wordButton.mindex + "",Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        mViewPan.clearColorFilter();
        super.onPause();
    }

    //播放动画总方法
    private void hangdlePlayButton(){
        if(mViewPanbar != null){
            if(!mIsRuning){
                mIsRuning = true;//播放后设置mIsRuning值,期间不能执行动画
                mViewPanbar.startAnimation(mBarInAnim);//播放动画
                mBtnPlaystart.setVisibility(View.INVISIBLE);//播放期间播放按钮隐藏
            }
        }

    }

    //根据当前关卡index获取歌曲文件名和歌曲名并返回
    private Song loadStageSongInfo(int stageIndex){
        Song song = new Song();
        String[] stage = Const.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);

        return  song;
    }

    private void initCurrentStageData(){
        //获取一个Song实例，初始化当前关的歌曲信息
        mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);

        //初始化已选文字
        mBtnSelectWords = initWordSelect();

        //创建LinearLayout控件用来装传入的Wordbutton控件，并且设置宽高
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140,140);

        for (int i = 0;i <mBtnSelectWords.size();i++){
            mViewWordsCantainer.addView(mBtnSelectWords.get(i).mViewButton,params);//创建Button控件
        }


        //从initAllword获取待选文字数据
        mAllWords = initAllword();
        //更新数据-MyGridView
        mMygridView.updateData(mAllWords);
    }


    //初始化待选文字框
    private ArrayList<WordButton> initAllword(){
        //生成所有单个字到容器并返回数据
        ArrayList<WordButton> data = new ArrayList<WordButton>();

        //获取所有待选文字
        String[] words = generateWoeds();

        for (int i = 0; i < MyGridView.COUNTS_WORD ; i++){
            WordButton button = new WordButton();
            button.mWordString = words[i];
            data.add(button);
        }
            return data;
    }

    //初始化已选文字框
    private ArrayList<WordButton> initWordSelect(){
        ArrayList<WordButton> data = new ArrayList<WordButton>();
        //获取控件，生成WordButton控件，并返回数据
        for (int i = 0; i < mCurrentSong.getNameLength();i ++){
            View view = Util.getView(MainActivity.this,R.layout.self_ui_gridview_item);

            WordButton holder = new WordButton();

            holder.mViewButton = (Button)view.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable = false;
            holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);

            data.add(holder);
        }
        return data;
    }

    //生成所有待选文字包括歌曲名
    private String[] generateWoeds(){

        Random random = new Random();//为了打乱顺序随机数使用
        String[] words = new String[MyGridView.COUNTS_WORD];

        //存入歌名
        for(int i  = 0; i<mCurrentSong.getNameLength(); i++){
            words[i] = mCurrentSong.getNameCharacters()[i] + "" ;//将歌曲名字字符串转为相应字符，将char类型转换为String
        }

        //除了歌名获取剩下的文字
        for (int i = mCurrentSong.getNameLength(); i<MyGridView.COUNTS_WORD; i++){
            words[i] = getRandomChar() + "";
        }

        //打乱文字在数组中的顺序,首先从所有元素中随机选取一个与第一个元素交换
        //然后在第二个之后选择一个元素与第二个交换，直到最后一个元素
        //这个算法保证每个元素在每个位置的概率都是1/n
        for(int i = MyGridView.COUNTS_WORD - 1; i >= 0 ;i--){
            int index = random.nextInt(i +1);//为了数是0-24

            String buf = words[index];
            words[index] = words [i];
            words[i] = buf;
        }



        return  words;

    }


    //生成随机汉字
    private char getRandomChar(){
        String str = "";
        int hightPos;
        int lowPos;
        //随机
        Random random = new Random();
        //生成高低位
        hightPos = (176 +Math.abs(random.nextInt(39)));
        lowPos = (161 + Math.abs(random.nextInt(93)));
        //创建容器装高低位组成汉子
        byte[] b =new byte[2];
        //强制整形变量
        b[0] = (Integer.valueOf(hightPos).byteValue());
        b[1] = (Integer.valueOf(lowPos).byteValue());

        try {
            //生成汉字
            str = new String(b,"GBK");

        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        //传char值
        return str.charAt(0);
    }

}
