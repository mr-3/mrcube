/*
 * Created on 2003/06/08
 *
 */
package mr3.data;

import java.io.*;

/**
 * @author takeshi morita
 * 
 * プロジェクトを保存するために作ったクラス
 * LiteralImplに相当するシリアライズ可能なクラスが見つかるまでは，
 * 一時的にこのクラスを使って，シリアライズすることにする．
 */
public class MR3LiteralImpl implements Serializable {
	String str;
	String lang;	
	
	MR3LiteralImpl(String s, String l) {
		str = s;
		lang = l;
	}
	
	public String getString() {
		return str;
	}
	
	public String getLanguage() {
		return lang;
	}
}
