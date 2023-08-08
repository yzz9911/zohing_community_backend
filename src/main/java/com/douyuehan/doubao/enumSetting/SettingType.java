package com.douyuehan.doubao.enumSetting;

public enum SettingType {

	BaseInfo("baseInfoForm","基本情報"),
	EmailInfo("emailForm","メール情報"),
	phoneInfo("phoneForm","携帯番号情報");
	
	private final String settingForm;
	private final String settingName;
	
	private SettingType(String settingForm,
			String settingName) {
		this.settingForm = settingForm;
		this.settingName = settingName;
	}
	
	public String getSettingForm() {
		return settingForm;
	}
	
	public String getSettingName() {
		return settingName;
	}
}
