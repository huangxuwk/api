package com.dl.multi_file.progress_bar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.swing.util.FontAndColor;
import com.swing.util.ISwing;

/**
 * 进度条界面类
 * @author dl
 *
 */
public class FileProgressBar extends FontAndColor implements ISwing {
	public static final int DEFAULT_BAR_PANE_WIDTH = 800;
	public static final int DEFAULT_BAR_PANE_HEIGHT = 105;
	public static final int DEFAULT_BAR_WIDTH = 750;
	public static final int DEFAULT_BAR_HEIGHT = 70;
	public static final int DEFAULT_MIN_PROGRESS = 0;
	public static final int DEFAULT_MAX_PROGRESS = 100;
	
	private JFrame jfrmMain;
	private JPanel jpnlMain;
	private boolean visible;

	private InnerProgressBar  innerProgressBar;

	public FileProgressBar() {
		initSwing();
	}
	
	@Override
	public void init() {
		jfrmMain = new JFrame("文件接收进度条栏");
		jfrmMain.setSize(DEFAULT_BAR_PANE_WIDTH, DEFAULT_BAR_PANE_HEIGHT);
		jfrmMain.setLocationRelativeTo(null);
		jfrmMain.setResizable(false);
		jfrmMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		jpnlMain = new JPanel(new BorderLayout());
		jfrmMain.add(jpnlMain);
	}
	
	/**
	 * 初始化
	 * @param fileNames
	 */
	public void initProgressBars(String fileName, int length) {
		innerProgressBar = new InnerProgressBar(length);
		innerProgressBar.initProgressBar(fileName);
	}
	
	/**
	 * 根据文件名去更新对应的进度条进度；
	 * @param fileName
	 * @param value
	 */
	public void updata(int value) {
		if (!visible) {
			showView();
			visible = true;
		}
		if (innerProgressBar != null) {
			if (!innerProgressBar.isAdd) {
				jpnlMain.add(innerProgressBar.jpanel, BorderLayout.CENTER);
				innerProgressBar.isAdd = true;
			}
			if (innerProgressBar.updataProgress(value)) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				closeView();
			}
		}
	}
	
	@Override
	public void reinit() {
	}

	@Override
	public void dealAction() {
		jfrmMain.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeView();
			}
		});
	}

	@Override
	public void showView() {
		jfrmMain.setVisible(true);
	}

	@Override
	public void closeView() {
		jfrmMain.dispose();
	}
	
	/**
	 * 内部进度条类<br>
	 * 1、提供初始化一个新的进度条的方法；<br>
	 * 2、提供改变进度的方法；
	 * @author dl
	 *
	 */
	class InnerProgressBar {
		private JPanel jpanel;
		private JProgressBar progressBar;

		private long length;
		private long curLen;
		private boolean isAdd;
		
		public InnerProgressBar(int length) {
			this.length = length;
		}
		
		/**
		 * 在进度条汇总界面初始化一个新的进度条
		 * @return
		 */
		public JPanel initProgressBar(String fileName) {
			jpanel = new JPanel(new BorderLayout());
			jpanel.setBackground(Color.white);
			jpanel.setSize(DEFAULT_BAR_PANE_WIDTH , DEFAULT_BAR_PANE_HEIGHT);
			UIManager.put("ProgressBar.selectionForeground",Color.BLACK);
			progressBar = new JProgressBar();
			progressBar.setSize(DEFAULT_BAR_PANE_WIDTH, DEFAULT_BAR_HEIGHT);
			progressBar.setMinimum(DEFAULT_MIN_PROGRESS);
			progressBar.setMaximum(DEFAULT_MAX_PROGRESS);
			progressBar.setStringPainted(true);
			progressBar.setBackground(Color.white);
			progressBar.setFont(smallFont);
			progressBar.setForeground(blueColor);
			TitledBorder ttbdInfo = new TitledBorder(" " + fileName + " - 接收进度 ");
			ttbdInfo.setTitleFont(smallFont);
			ttbdInfo.setTitleColor(fontColor);
			ttbdInfo.setTitleJustification(TitledBorder.CENTER);
			progressBar.setBorder(ttbdInfo);
			progressBar.setBorder(ttbdInfo);
			
			jpanel.add(progressBar, BorderLayout.CENTER);
			return jpanel;
		}
		
		/**
		 * 计算已接收字节数占全部接收字节数的比值，设置进度条数据
		 * @param len
		 * @return
		 */
		public boolean updataProgress(int len) {
			curLen += len;
			int value = (int) ((100 * curLen) / length);
			progressBar.setValue(value);
			if (value == DEFAULT_MAX_PROGRESS) {
				return true;
			}
			return false;
		}
		
	}
	
}
