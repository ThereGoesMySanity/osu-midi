package Main;

import javax.swing.DefaultListSelectionModel;

@SuppressWarnings("serial")
class DisabledItemSelectionModel extends DefaultListSelectionModel {

    @Override
    public void setSelectionInterval(int index0, int index1) {
        super.setSelectionInterval(-1, -1);
    }
}