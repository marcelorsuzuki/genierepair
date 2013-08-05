package genierepair.views;

import java.util.Set;

import javax.swing.JOptionPane;

import tmp.MySingleResult;
import genierepair.pools.AddedSlices;
import genierepair.testing.MyMethodInterface;
import genierepair.util.EntitiesUtil;

public class SliceRemoveDialog {

	public static Long showDialog(long def){
		SliceRemoveObject[] options = makeOptions(def);
		if(options.length==0){
			JOptionPane.showMessageDialog(null, "There is no slice to be removed");
		} else {
			Object choice = JOptionPane.showInputDialog(null, "Which slice would you like to remove?", "Please select",JOptionPane.DEFAULT_OPTION ,null, options,options[0]);
			if(choice!=null && choice instanceof SliceRemoveObject){
				return ((SliceRemoveObject)choice).getID();
			}
		}
		
		return -1L;
	}

	private static SliceRemoveObject[] makeOptions(long def) {
		Set<MySingleResult> set = AddedSlices.getKeys();
		SliceRemoveObject[] vector = new SliceRemoveObject[set.size()];
		int i=0;
		SliceRemoveDialog aux = new SliceRemoveDialog();
		int idx=0;
		for(MySingleResult sr : set){
			if(sr.getEntityID().equals(def)){
				idx=i;
			}
			vector[i++]=aux.new SliceRemoveObject(AddedSlices.get(sr),sr);
		}
		if(vector.length>0){
			SliceRemoveObject tmp = vector[idx];
			vector[idx]=vector[0];
			vector[0]=tmp;
		}
		return vector;
	}

	public class SliceRemoveObject{
		private String text;
		private Long id;

		public SliceRemoveObject(MyMethodInterface mi,MySingleResult sr){
			text = EntitiesUtil.shortNamesOnJavaAPI(sr) + " used to fix " +mi;
			id=sr.getEntityID();
		}

		public String toString(){return text; }
		public Long getID(){return id;}
	}
}
