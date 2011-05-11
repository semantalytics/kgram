package fr.inria.acacia.corese.gui.event;

import fr.inria.acacia.corese.gui.core.MainFrame;
import fr.inria.edelweiss.kgram.event.EvalListener;


/**
 * 
 * KGRAM Eval Listener
 * Interact with GUI through a synchronized buffer
 *
 */
public class MyEvalListener extends EvalListener {
	MainFrame frame;
	
	public static MyEvalListener create(){
		MyEvalListener el = new MyEvalListener();
		return el;
	}
	
	public void setFrame(MainFrame mf){
		frame = mf;
	}
	
	public void log(Object obj){
		String str = obj.toString();
		frame.appendMsg(str);
	}
	

	
}
