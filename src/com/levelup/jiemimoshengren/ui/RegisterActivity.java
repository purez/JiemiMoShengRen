/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.levelup.jiemimoshengren.ui;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dd.circularprogressbutton.CircularProgressButton;
import com.levelup.jiemimoshengren.R;
import com.levelup.jiemimoshengren.base.DefaultActivity;
import com.levelup.jiemimoshengren.config.Constant;
import com.levelup.jiemimoshengren.utils.FileUtil;
import com.smy.volley.extend.EasyJsonObject;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends DefaultActivity {
	private EditText userNameEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private CircularProgressButton registerBtn;
	private ImageView headIv; // 头像
	private Bitmap headBmp; // 头像bitmap
	private RadioGroup rgSex; // 性别选择

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_register);
	}

	@Override
	protected void initData() {
		super.initData();
	}

	@Override
	protected void initView() {
		super.initView();
		userNameEditText = (EditText) findViewById(R.id.username);
		passwordEditText = (EditText) findViewById(R.id.password);
		confirmPwdEditText = (EditText) findViewById(R.id.confirm_password);
		headIv = (ImageView) findViewById(R.id.img_head);
		rgSex = (RadioGroup) findViewById(R.id.rg_sex);
		registerBtn = (CircularProgressButton) findViewById(R.id.register);
		registerBtn.setIndeterminateProgressMode(true);
		resizeImg();
	}

	/** 适配图片 */
	private void resizeImg() {
		View regiBack = findViewById(R.id.layout_title);
		Bitmap bmp = adaptiveToScreenWidth(BitmapFactory.decodeResource(
				getResources(), R.drawable.register_back));
		regiBack.setBackgroundDrawable(new BitmapDrawable(getResources(), bmp));
		LayoutParams layoutParams = regiBack.getLayoutParams();
		layoutParams.height = bmp.getHeight();
		regiBack.setLayoutParams(layoutParams);

		View headBack = findViewById(R.id.img_circle);
		layoutParams = headBack.getLayoutParams();
		bmp = adaptive(R.drawable.head_circle, (int) (bmp.getHeight() * 0.67),
				(int) (bmp.getHeight() * 0.67));
		headBack.setBackgroundDrawable(new BitmapDrawable(getResources(), bmp));
		layoutParams.height = (int) (bmp.getHeight() * 0.67);
		layoutParams.width = layoutParams.height;
		headBack.setLayoutParams(layoutParams);

		bmp = adaptive(R.drawable.default_head, bmp.getWidth(), bmp.getHeight());
		headIv.setImageBitmap(bmp);
		headIv.setLayoutParams(layoutParams);
	}

	/** 处理volley请求错误 */
	@Override
	public void onErrorResponse(VolleyError error) {
		super.onErrorResponse(error);
		registerBtn.setProgress(0);
		registerBtn.setClickable(true);
	}

	/** 跳到选择图片的界面 */
	public void selectImg(View view) {
		startActivityForResult(new Intent(this, SelectImgPopupActivity.class),0);
	}

	// 注册
	public void register(View view) {
		final String username = userNameEditText.getText().toString().trim();
		final String pwd = passwordEditText.getText().toString().trim();
		String confirm_pwd = confirmPwdEditText.getText().toString().trim();
		if (TextUtils.isEmpty(username)) {
			showMsgFromRes(R.string.User_name_cannot_be_empty);
			userNameEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(pwd)) {
			showMsgFromRes(R.string.Password_cannot_be_empty);
			passwordEditText.requestFocus();
			return;
		} else if (TextUtils.isEmpty(confirm_pwd)) {
			showMsgFromRes(R.string.Confirm_password_cannot_be_empty);
			confirmPwdEditText.requestFocus();
			return;
		} else if (!pwd.equals(confirm_pwd)) {
			showMsgFromRes(R.string.Two_input_password);
			return;
		}

		if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
			if (headBmp == null) { // 加载默认图片
				headBmp = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_launcher);
			}
			String sex = rgSex.getCheckedRadioButtonId() == R.id.rb_male ? getString(R.string.hint_sex_male)
					: getString(R.string.hint_sex_female);
			registerBtn.setProgress(50);
			registerBtn.setClickable(false);
			registerUser(username, pwd, sex, getString(R.string.sign_hint) + username,
					FileUtil.base64EncodeImg(headBmp));
		}
	}

	/** 注册用户 */
	public void registerUser(final String nick, final String psw,
			final String sex, final String sign, final String img) {
		JSONObject regiJson = makeRegiJson(nick, psw, sex, sign, img);
		this.requestQueue.add(new JsonObjectRequest(Constant.URL_REGISTER,
				regiJson, new Listener<JSONObject>() {
					public void onResponse(JSONObject response) {
						EasyJsonObject easyResp = new EasyJsonObject(response);
						boolean success = easyResp.getBoolean("success");
						if (success) { // 注册成功
							registerBtn.setProgress(100);
							showMsgFromRes(R.string.Registered_successfully);
							onRegisterSuccess(); // 返回到登录界面
						} else { // 注册失败
							registerBtn.setProgress(0);
							registerBtn.setClickable(true);
							showMsg(easyResp.getString("error")); // 显示错误信息
						}
					}
				}, this));
	}

	/**
	 * 生成注册用的jsonobject
	 */
	private JSONObject makeRegiJson(String nick, String psw, String sex,
			String sign, String img) {
		EasyJsonObject json = new EasyJsonObject();
		json.put("nick", nick);
		json.put("password", psw);
		json.put("sex", sex);
		json.put("sign", sign);
		json.put("head", img);
		return json;
	}

	/** 登录成功回传数据给登录界面 */
	private void onRegisterSuccess() {
		Intent intent = new Intent();
		intent.putExtra("username", userNameEditText.getText().toString());
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(data!=null){
			Bundle bundle = data.getExtras();
			if(bundle!=null){
				headBmp = bundle.getParcelable("data");
				headIv.setImageBitmap(headBmp);
			}
		}
	}
}
