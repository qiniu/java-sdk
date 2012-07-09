import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import org.json.JSONObject;

import com.qiniu.qbox.Config;
import com.qiniu.qbox.up.BlockProgress;
import com.qiniu.qbox.up.BlockProgressNotifier;
import com.qiniu.qbox.up.ProgressNotifier;

public class ResumableGUINotifier implements ProgressNotifier,
		BlockProgressNotifier {

	private PrintStream os;
	private long current;  
	private long amount;
	private long lastTime; //the timestamp of last chunck upload
	private long realSpeed; //the speed of the file upload
	// ����һ����ֱ������
	private JProgressBar bar;
	private JLabel realTimeSpeedLabel = new JLabel();
	JFrame frame;
	boolean pauseFlag = false;

	public ResumableGUINotifier(String progressFile, BlockProgress[] progresses, long fileSize) throws Exception {

		OutputStream out = new FileOutputStream(progressFile, true);
		this.os = new PrintStream(out, true);
		long currentSize = 0;
		for (int i = 0; i < progresses.length; i++) {
			if (progresses[i] != null) {
				currentSize += progresses[i].offset;
			}
		}

		this.current = currentSize;
		this.amount = fileSize;
		this.realSpeed = 0;
		this.frame = new JFrame("�ϵ���������");
		// ����һ����ֱ������
		this.bar = new JProgressBar(JProgressBar.HORIZONTAL);

		final JButton terminal = new JButton("�˳�");

		Box funcBox = new Box(BoxLayout.Y_AXIS);
		Box infoBox = new Box(BoxLayout.Y_AXIS);
		Box realSpeedBox = new Box(BoxLayout.X_AXIS);

		funcBox.add(terminal);

		realSpeedBox.add(new JLabel("�ϴ���ʱ�ٶȣ�"));
		realSpeedBox.add(this.realTimeSpeedLabel);
		
		infoBox.add(this.bar);
		infoBox.add(new JLabel("�ļ���С��" + fileSize/(1024 * 1024) + "M"),1);
		infoBox.add(realSpeedBox);
		
		this.frame.setLayout(new FlowLayout());
		this.frame.add(infoBox);
		this.frame.add(funcBox);


		// �����ڽ������л�����ɰٷֱ�
		this.bar.setStringPainted(true);

		// ���ý����������ֵ����Сֵ,
		this.bar.setMinimum(0);
		// ������������Ϊ�����������ֵ
		this.bar.setMaximum((int) this.amount);
		Timer timer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// ������ĵ�ǰ��������ý�������value
				bar.setValue((int) current);
				realTimeSpeedLabel.setText(realSpeed/1024 + "KB/s");
				if(bar.getPercentComplete() == 1.0) {
					frame.dispose();
				}
			}
		});
		timer.start();

		terminal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.pack();
		this.frame.setVisible(true);

		this.lastTime = System.currentTimeMillis();
	}

	@Override
	public void notify(int blockIndex, String checksum) {

		try {
			HashMap<String, Object> doc = new HashMap<String, Object>();
			doc.put("block", blockIndex);
			doc.put("checksum", checksum);
			String json = JSONObject.valueToString(doc);
			os.println(json);
			System.out.println("Progress Notify:" + "\n\tBlockIndex: "
					+ String.valueOf(blockIndex) + "\n\tChecksum: " + checksum);
		} catch (Exception e) {
			// nothing to do;
		}
	}

	@Override
	public void notify(int blockIndex, BlockProgress progress) {

		try {
			this.realSpeed = Config.PUT_CHUNK_SIZE / (System.currentTimeMillis() - this.lastTime) * 1000;
			if (this.pauseFlag) {
				wait();
			}
			HashMap<String, Object> doc = new HashMap<String, Object>();
			doc.put("block", blockIndex);

			Map<String, String> map = new HashMap<String, String>();
			map.put("context", progress.context);
			map.put("offset", progress.offset + "");
			map.put("restSize", progress.restSize + "");
			doc.put("progress", map);

			String json = JSONObject.valueToString(doc);
			os.println(json);

			this.current = (int) this.current + Config.PUT_CHUNK_SIZE;		
			this.lastTime = System.currentTimeMillis();
		} catch (Exception e) {
			// nothing to do;
		}
	}

}
