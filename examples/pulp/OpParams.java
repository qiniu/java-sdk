import java.util.List;

/**
 * construct OpParams in OpChild
 * 
 * @author gebo
 *
 */
public class OpParams {
	public List<OpParamsLabels> labels;
	public Terminate terminate;

	public OpParams() {
		super();
	}

	public OpParams(List<OpParamsLabels> labels, Terminate terminate) {
		super();
		this.labels = labels;
		this.terminate = terminate;
	}

	public List<OpParamsLabels> getLabels() {
		return labels;
	}

	public void setLabels(List<OpParamsLabels> labels) {
		this.labels = labels;
	}

	public Terminate getTerminate() {
		return terminate;
	}

	public void setTerminate(Terminate terminate) {
		this.terminate = terminate;
	}

}
