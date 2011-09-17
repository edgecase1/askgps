package at.catnet.askgps;

import android.util.Log;

public class StateManager implements Runnable {
	
	public static final String TAG = "StateManager";
	
	public static final int TIME_TO_STAY = 10;
	public static final int TIME_TO_IDLE = 20;
	public static final int WAIT_INTERVAL = 5;
	public static final int TIME_MAX = TIME_TO_STAY + TIME_TO_IDLE + WAIT_INTERVAL;
	
	int time;
	public IState state;
	
	private Journal journal;
	
	public StateManager(Journal j) {
		state = new StateUnknown();
		journal = j;
		
		Thread t = new Thread(this);
		t.start();
	}
	
	public synchronized IState getState(){
		return state;
	}
	
	public synchronized void move(){
		time = 0;
		changeState(new StateMoving());
	}
	
	public synchronized void changeState(IState s){
		Log.d(TAG, "changeState " + state + "=>" + s);
		// skip if equal
		if(state.equals(s)){
			Log.v(TAG, "state is the same (" + state + "==" + s + ")");
			return;
		}
		// change state
		Log.i(TAG, "changing state from " + state + " to " + s);
		state = s;
		// to a state-specific action
		state.handle(journal);
	}
	
	public synchronized void outdate(){
		
		if(time >= TIME_TO_IDLE){
			changeState(new StateIdle());
		} else if(time >= TIME_TO_STAY){
				changeState(new StateStay());
		}
		
		// count up
		if(time <= TIME_MAX){
			time += WAIT_INTERVAL;	
		}
	}
	
	@Override
	public void run() {
		time = 0;
		while(true){
			outdate();
			// fall asleep and wait
			try {
				Thread.currentThread().sleep(WAIT_INTERVAL*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}


interface IState {
	public int getCode();
	public void handle(Journal j);
}

abstract class AbstractState implements IState {
	
	@Override
	public boolean equals(Object o) {
		return ((IState)o).getCode() == this.getCode();
	}
	
}

class StateMoving extends AbstractState  {
	
	private final int code = 1;
	
	@Override
	public void handle(Journal j) {
		
	}
	
	@Override
	public String toString() {
		return "moving";
	}

	@Override
	public int getCode() {
		return code;
	}
	
}

class StateStay extends AbstractState {
	
	private final int code = 0;

	@Override
	public void handle(Journal j) {
		
	}
	
	@Override
	public String toString() {
		return "staying";
	}
	
	@Override
	public int getCode() {
		return code;
	}
}

class StateIdle extends AbstractState {
	
	private final int code = 2;

	@Override
	public void handle(Journal j) {
		j.putPoi();
	}
	
	@Override
	public String toString() {
		return "idle";
	}
	
	@Override
	public int getCode() {
		return code;
	}
}

class StateUnknown extends AbstractState {
	
	private final int code = -1;

	@Override
	public void handle(Journal j) {
		
	}
	
	@Override
	public String toString() {
		return "unknown";
	}
	
	@Override
	public int getCode() {
		return code;
	}
	
}


