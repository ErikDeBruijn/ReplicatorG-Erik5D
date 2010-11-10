package replicatorg.app.ui.modeling;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.media.j3d.Transform3D;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import net.miginfocom.swing.MigLayout;
import replicatorg.app.Base;
import replicatorg.app.ui.modeling.PreviewPanel.DragMode;

public class MoveTool extends Tool implements KeyListener {
	public MoveTool(ToolPanel parent) {
		super(parent);
	}
	
	Transform3D vt;

	public Icon getButtonIcon() {
		return null;
	}

	public String getButtonName() {
		return "Move";
	}

	JCheckBox lockZ;
	boolean zWasLocked = false;
	boolean manualLockZ = false;
	double dragStartBottom;
	
	public JPanel getControls() {
		JPanel p = new JPanel(new MigLayout("fillx,filly"));
		JButton centerButton = createToolButton("Center","images/center-object.png");
		centerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.getModel().center();
			}
		});
		p.add(centerButton,"growx,wrap");

		JButton lowerButton = createToolButton("Put on platform","images/center-object.png");
		lowerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.getModel().putOnPlatform();
			}
		});
		p.add(lowerButton,"growx,wrap");

		lockZ = new JCheckBox("Lock height");
		lockZ.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//Base.logger.info("modifiers: "+e.getModifiers());
//				e.getModifiers()// ActionEvent.MOUSE_EVENT_MASK
				if(e.getModifiers()==16)
				{
					manualLockZ = lockZ.isSelected();
					Base.logger.info("Manually (un)locked. ManLock="+manualLockZ);
				}
			}
		});
		p.add(lockZ,"growx,wrap");
		
		return p;
	}

	public String getInstructions() {
		return Base.isMacOS()?
				"<html><body>Drag to move object<br>Shift-drag to constrain height<br>Mouse wheel to zoom</body></html>":
				"<html><body>Left drag to move object<br>Right drag to rotate view<br>Mouse wheel to zoom</body></html>";
	}

	public String getTitle() {
		return "Move Object";
	}

	public void mouseDragged(MouseEvent e) {
		if (startPoint == null) 
		{
			return;
		}
		Point p = e.getPoint();
		mode = DragMode.NONE; 
//		if (Base.isMacOS()) {
//			if (button == MouseEvent.BUTTON1 && !e.isShiftDown()) { mode = DragMode.TRANSLATE_OBJECT; }
//		} else {
//		if (button == MouseEvent.BUTTON2) { mode = DragMode.NONE; }
			if (button == MouseEvent.BUTTON1) { mode = DragMode.TRANSLATE_OBJECT; }
//		}
		double xd = (double)(p.x - startPoint.x);
		double yd = -(double)(p.y - startPoint.y);
		switch (mode) {
		case NONE:
			super.mouseDragged(e);
			break;
		case TRANSLATE_OBJECT:
			doTranslate(xd,yd);
			break;
		}
		startPoint = p;
	}

	
	public void mousePressed(MouseEvent e) {
		Point3d bottom = parent.getModel().getBottom();
		Base.logger.info("drag start = "+bottom.z);
		dragStartBottom = bottom.z;

		// Set up view transform
		if(e.isShiftDown()){
			Base.logger.info("Locked z. ManualLock="+manualLockZ);
			lockZ.setSelected(true);
			zWasLocked = true;
		} else {
			if(manualLockZ==false)
			{
				lockZ.setSelected(false);
				zWasLocked = false;
			}
		}
		vt = parent.preview.getViewTransform();
		super.mousePressed(e);
	}
	
	void doTranslate(double deltaX, double deltaY) {
		Vector3d v = new Vector3d(deltaX,deltaY,0d);
		vt.transform(v);
		if (lockZ.isSelected()) { 
		//	v.z = 0d; 
			Point3d bottom = parent.getModel().getBottom();
			v.z = dragStartBottom - bottom.z; 
//			Base.logger.info("Drag start bottom = "+dragStartBottom);
		}
		parent.getModel().translateObject(v.x,v.y,v.z);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.isShiftDown())
		{
			Base.logger.info("Shift first pressed. Should not orbit. ManualLock="+manualLockZ);
			lockZ.setSelected(true);
			
			zWasLocked = true;
		} else {
			if(manualLockZ==false)
				zWasLocked = lockZ.isSelected();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(!e.isShiftDown())
		{
			Base.logger.info("Shift released. ManualLock="+manualLockZ);
			if(manualLockZ==false)
				lockZ.setSelected(false);
		}
		
	}

}
