package mr3.ui;
import java.awt.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

/**
 *
 * upperList ... nameSpaces
 * lowerList ... localNames
 *
 * RDFPropertyPanelに組み込んだ．
 *
 * @auther takeshi morita
 */
public class SelectPropertyDialog extends SelectListDialog {

	private Map propMap;
	private List validPropertyList;
	private IconCellRenderer renderer;

	public SelectPropertyDialog() {
		super(null, "Select Property");
		renderer = new IconCellRenderer();
		lowerList.setCellRenderer(renderer);
	}

	/** イメージ付きリストを描画 */
	class IconCellRenderer extends JLabel implements ListCellRenderer {

		private List validConfirmList;

		IconCellRenderer() {
			setOpaque(true);
		}

		public void setValidConfirmList(List list) {
			validConfirmList = list;
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			String str = value.toString();
			setText(str);

			Boolean isValid = (Boolean) validConfirmList.get(index);
			if (isValid.booleanValue()) {
//				URL valid = MR3Resource.getImageIcon("valid.gif");
				URL valid = this.getClass().getClassLoader().getResource("mr3/resources/valid.gif");
				setIcon(new ImageIcon(valid));
			} else {
				setIcon(null);
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());

			return this;
		}
	}

	public void setProperties(Set nameSpaces, Map pmap, List vlist) {
		upperList.setListData(nameSpaces.toArray());
		propMap = pmap;
		validPropertyList = vlist;
	}

	private static final String NULL_LOCAL_NAME = "(Null)";

	private void selectUpperList() {
		if (!lowerList.isSelectionEmpty()) {
			lowerList.clearSelection();
		}
		String nameSpace = (String) upperList.getSelectedValue();
		Set localNames = (Set) propMap.get(nameSpace);
		if (nameSpace != null) {
			Set modifyLocalNames = new HashSet();
			for (Iterator i = localNames.iterator(); i.hasNext();) {
				String localName = (String) i.next();
				if (localName.length() == 0) { // localNameがない場合，Nullを表示
					modifyLocalNames.add(NULL_LOCAL_NAME);
				} else {
					modifyLocalNames.add(localName);
				}
			}
			setRenderer(nameSpace, modifyLocalNames);
			lowerList.setListData(modifyLocalNames.toArray());
		}
	}

	private void selectLowerList() {
		if (upperList.getSelectedValue() != null && lowerList.getSelectedValue() != null) {
			String ns = upperList.getSelectedValue().toString();
			String ln = lowerList.getSelectedValue().toString();
			if (ln.equals(NULL_LOCAL_NAME)) {
				ln = "";
			}
			uri.setText(ns + ln);
			uri.setToolTipText(ns + ln);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		try {
			if (e.getSource() == upperList) {
				selectUpperList();
			} else if (e.getSource() == lowerList) {
				selectLowerList();
			}
		} catch (NullPointerException np) { //あとではずす
			np.printStackTrace();
		}
	}

	private static Boolean TRUE = new Boolean(true);
	private static Boolean FALSE = new Boolean(false);

	private void setRenderer(String nameSpace, Set localNames) {
		List list = new ArrayList();
		for (Iterator i = localNames.iterator(); i.hasNext();) {
			String uri = nameSpace + i.next();
			Resource res = new ResourceImpl(uri);
			if (validPropertyList.contains(res)) {
				list.add(TRUE);
			} else {
				list.add(FALSE);
			}
		}
		renderer.setValidConfirmList(list);
	}

}
