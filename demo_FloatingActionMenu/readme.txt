author:wangzhiwen
android studio项目
date：创建日期 2015-11-13 
version：demo版本（含versionCode,versionName）
buildingFor: 悬浮弹出式菜单
minSDK：Android 2.3 及以上版本

description of the project：项目的详细介绍和试用说明
	主要类FloatingActionMenu
	使用xml布局添加此FloatingActionMenu，需加入attrs属性xml 
	1、可设置弹出方向（代码设置或xml设置）floatingActionMenu.setmExpandDirection（）
	2、菜单第一个view用于点击展开或收起，在loatingActionMenu类的createBaseView（）中添加或加载布局进去（有空再弄出来）
	3、添加弹出菜单item  floatingActionMenu.addActionsView（view）
	4、其他待加
