package com.justdoit.pics.adapater;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.justdoit.pics.R;
import com.justdoit.pics.activity.ChangeInfoActivity;
import com.justdoit.pics.activity.UserInfoActivity;
import com.justdoit.pics.bean.UserInfo;
import com.justdoit.pics.bean.UserRelationListInfo;
import com.justdoit.pics.global.App;
import com.justdoit.pics.global.Constant;
import com.justdoit.pics.model.NetSingleton;
import com.justdoit.pics.widget.PersonalIntroItemView;

/**
 * Created by mengwen on 2015/11/10.
 */
public class BriefIntroAdapter extends RecyclerView.Adapter {

    private final static String TAG = "BriefIntroAdapter";
    // 传递给ChangeInfoActivty的参数Key
    private final static String KEY_OLD_VALUE = "oldValue";
    private final static String KEY_NAME_TYPE = "nameType";

    private final static int FOLLOW_NUM = 10; // 10个人头像

    private UserInfo userInfo = null;
    private UserRelationListInfo followingList = null;
    private UserRelationListInfo followerList = null;

    private boolean isUserOwn = true;
    private int NUM_ITEM = 0;
    private Context context;
    private View.OnClickListener briefItemClickListener;

    // 简介页面item
    public static enum NAME_TYPE {
        nickname("nickname"),
        email("email"),
        sex("sex"),
        residence(""), // 居住地
        birthday("birthday");

        private String name;

        NAME_TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    // item类型
    private enum ITEM_TYPE {
        ITEM_PERSONAL_INTRO, // 基本信息
        ITEM_CONNECTIONS // 人脉
    }

    public BriefIntroAdapter(Context context) {
        this(context, true);
    }

    public BriefIntroAdapter(final Context context, boolean isUserOwn) {

        this.isUserOwn = isUserOwn;
        this.context = context;
        userInfo = new UserInfo();
        NUM_ITEM = ITEM_TYPE.values().length; // 基本信息和人脉

        briefItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChangeInfoActivity.class);
                Bundle args = new Bundle();
                PersonalIntroItemView piv = (PersonalIntroItemView) v;

                args.putString(KEY_OLD_VALUE, piv.getContent());

                switch (piv.getId()) {
                    case R.id.personal_intro_nickname:
                        args.putInt(KEY_NAME_TYPE, NAME_TYPE.nickname.ordinal());
                        break;
                    case R.id.personal_intro_email:
                        args.putInt(KEY_NAME_TYPE, NAME_TYPE.email.ordinal());
                        break;
                    case R.id.personal_intro_birthday:
                        args.putInt(KEY_NAME_TYPE, NAME_TYPE.birthday.ordinal());
                        break;
                    case R.id.personal_intro_residence:
                        args.putInt(KEY_NAME_TYPE, NAME_TYPE.residence.ordinal());
                        break;
                    case R.id.personal_intro_sex:
                        args.putInt(KEY_NAME_TYPE, NAME_TYPE.sex.ordinal());
                        break;
                    default:
                        break;
                }

                intent.putExtras(args);

                context.startActivity(intent);
            }
        };
    }

    /**
     * 设置user info和更新item数量
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setFollowings(UserRelationListInfo followings) {
        this.followingList = followings;
    }

    public void setFollowers(UserRelationListInfo followers) {
        this.followerList = followers;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder holder;

        if (viewType == ITEM_TYPE.ITEM_PERSONAL_INTRO.ordinal()) {
            holder = new PersonalHolder((LayoutInflater.from(parent.getContext())).inflate(R.layout.brief_intro_personal, parent, false));
        } else {
            holder = new ConnectionsHolder((LayoutInflater.from(parent.getContext())).inflate(R.layout.brief_intro_connections, parent, false), isUserOwn);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Resources res = context.getResources();


        if (holder instanceof PersonalHolder) {
            // 通过isUserOwn判断是否可以编辑
            // 名称 + 值 + 可否编辑(Y/N)
            // 昵称 Y
            ((PersonalHolder) holder).nicknamePIV.update(res.getString(R.string.nick_name), userInfo.getNickname(), isUserOwn);
            ((PersonalHolder) holder).nicknamePIV.setOnClickListener(briefItemClickListener);
            // email Y
            ((PersonalHolder) holder).emailPIV.update(res.getString(R.string.prompt_email), userInfo.getEmail(), isUserOwn);
            ((PersonalHolder) holder).emailPIV.setOnClickListener(briefItemClickListener);

            // 性别 Y
            if ("1".equals(userInfo.getSex())) {
                ((PersonalHolder) holder).sexPIV.update(res.getString(R.string.sex), res.getString(R.string.man), isUserOwn);
            } else if ("0".equals(userInfo.getSex())) {
                ((PersonalHolder) holder).sexPIV.update(res.getString(R.string.sex), res.getString(R.string.female), isUserOwn);
            } else {
                ((PersonalHolder) holder).sexPIV.update(res.getString(R.string.sex), res.getString(R.string.unknown), isUserOwn);
            }
            ((PersonalHolder) holder).sexPIV.setOnClickListener(briefItemClickListener);

            // 居住地 Y
            ((PersonalHolder) holder).residencePIV.update(res.getString(R.string.user_info_location), userInfo.getCountry() + " " + userInfo.getProvince() + " " + userInfo.getCity(), isUserOwn);
            ((PersonalHolder) holder).residencePIV.setOnClickListener(briefItemClickListener);

            // 生日 Y TODO:没有进行时间格式转换
            if (userInfo.getBirthday() == null) {
                ((PersonalHolder) holder).birthdayPIV.update(res.getString(R.string.birthday), "", isUserOwn);
            } else {
                ((PersonalHolder) holder).birthdayPIV.update(res.getString(R.string.birthday), String.valueOf(userInfo.getBirthday()), isUserOwn);
            }
            ((PersonalHolder) holder).birthdayPIV.setOnClickListener(briefItemClickListener);


        } else if (holder instanceof ConnectionsHolder) {
            ImageLoader imageLoader = NetSingleton.getInstance(context).getImageLoader();

            if (followingList != null) {

                // 关注列表
                ((ConnectionsHolder) holder).followingNum.setText(String.valueOf(followingList.getCount()));
                for (int i = 0; i < followingList.getCount() && i < FOLLOW_NUM; i++) {

                    final UserRelationListInfo.ResultsEntity.RelationUserEntity userEntity = followingList.getResults().get(i).getRelation_user();
                    NetworkImageView item = new NetworkImageView(context);
                    item.setDefaultImageResId(R.drawable.ic_image_black_48dp);
                    item.setErrorImageResId(R.drawable.ic_broken_image_black_48dp);
                    item.setImageUrl(String.valueOf(userEntity.getAvatar()), imageLoader);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, UserInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt(Constant.USER_ID_NAME, userEntity.getId());
                            bundle.putString(Constant.USERNAME_NAME, userEntity.getUsername());
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                    });
                    ((ConnectionsHolder) holder).followingLayout.addView(item, i);
                }
            }

            if (followerList != null) {
                // 粉丝列表
                ((ConnectionsHolder) holder).followerNum.setText(String.valueOf(followerList.getCount()));
                for (int i = 0; i < followerList.getCount() && i < FOLLOW_NUM; i++) {
                    final UserRelationListInfo.ResultsEntity.RelationUserEntity userEntity = followerList.getResults().get(i).getRelation_user();
                    NetworkImageView item = new NetworkImageView(context);
                    item.setDefaultImageResId(R.drawable.ic_image_black_48dp);
                    item.setErrorImageResId(R.drawable.ic_broken_image_black_48dp);
                    item.setImageUrl(String.valueOf(followerList.getResults().get(i).getRelation_user().getAvatar()), imageLoader);
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, UserInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt(Constant.USER_ID_NAME, userEntity.getId());
                            bundle.putString(Constant.USERNAME_NAME, userEntity.getUsername());
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                        }
                    });
                    ((ConnectionsHolder) holder).followerLayout.addView(item, i);
                }
            }
        } else {

        }

    }

    @Override
    public int getItemViewType(int position) {
        // 根据位置返回item类型
        // 默认返回个人基本信息类型
        return ITEM_TYPE.values()[position].ordinal();
    }

    @Override
    public int getItemCount() {
        return NUM_ITEM;
    }

    // 人脉view holder
    class ConnectionsHolder extends RecyclerView.ViewHolder {

        TextView titleTv;
        TextView followerNum;
        TextView followingNum;
        LinearLayout followerLayout;
        LinearLayout followingLayout;

        public ConnectionsHolder(View itemView, boolean isVisible) {
            super(itemView);

            // 设置为不可见
            if (!isVisible) {
                itemView.setVisibility(View.GONE);
            }
            titleTv = (TextView) itemView.findViewById(R.id.brief_connections_intro_title);
            followingLayout = (LinearLayout) itemView.findViewById(R.id.following_layout);
            followerLayout = (LinearLayout) itemView.findViewById(R.id.follower_layout);
            followerNum = (TextView) itemView.findViewById(R.id.follower_num);
            followingNum = (TextView) itemView.findViewById(R.id.following_num);
        }
    }

    // 个人基本信息view holder
    class PersonalHolder extends RecyclerView.ViewHolder {

        TextView titleTv; // 标题

        PersonalIntroItemView sexPIV; // 性别栏
        PersonalIntroItemView birthdayPIV; // 生日栏
        PersonalIntroItemView residencePIV; // 居住地址
        PersonalIntroItemView emailPIV; // 邮箱
        PersonalIntroItemView nicknamePIV; // 用户名

        public PersonalHolder(View itemView) {
            super(itemView);

            titleTv = (TextView) itemView.findViewById(R.id.brief_personal_intro_title);
            sexPIV = (PersonalIntroItemView) itemView.findViewById(R.id.personal_intro_sex);
            birthdayPIV = (PersonalIntroItemView) itemView.findViewById(R.id.personal_intro_birthday);
            residencePIV = (PersonalIntroItemView) itemView.findViewById(R.id.personal_intro_residence);
            emailPIV = (PersonalIntroItemView) itemView.findViewById(R.id.personal_intro_email);
            nicknamePIV = (PersonalIntroItemView) itemView.findViewById(R.id.personal_intro_nickname);
        }
    }
}
