package mr3.data;
/**
 * CellをURIで表示するか，ラベルで表示するかを決める．
 *
 */
public class CellViewType {
	private String type;
    
	private CellViewType(String t) {
		type = t;
	}

	public static final CellViewType URI = new CellViewType("URI");
	public static final CellViewType LABEL = new CellViewType("Label");
	
	public String toString() {
		return type;
	}
}
