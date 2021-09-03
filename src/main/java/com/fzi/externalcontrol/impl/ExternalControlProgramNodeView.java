//-- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2019 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//-- END LICENSE BLOCK ------------------------------------------------

//----------------------------------------------------------------------
/*!\file
*
* \author  Lea Steffen steffen@fzi.de
* \date    2019-04-18
*
*/
//----------------------------------------------------------------------

package com.fzi.externalcontrol.impl;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Dimension;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

public class ExternalControlProgramNodeView
    implements SwingProgramNodeView<ExternalControlProgramNodeContribution> {
  private JCheckBox advancedParam_CB;
  private String description = "";
  private JLabel infoLabel;
  private JPanel standardParamsPanel;
  private JPanel advancedParamsPanel;
  private JTextField maxLostPackages_TF = new JTextField(15);
  private JTextField gainServoj_TF = new JTextField(15);

  // public ExternalControlProgramNodeView(ViewAPIProvider apiProvider) {}

  // @Override
  // public void buildUI(
  //     JPanel panel, ContributionProvider<ExternalControlProgramNodeContribution> provider) {
  //   panel.setLayout(new GridLayout(2, 1, 5, 5));
  //   infoLabel = new JLabel();
  //   infoLabel.setFont(new Font("Serif", Font.BOLD, 14));
  //   panel.add(infoLabel);

    /////////////////////////////
    public ExternalControlProgramNodeView(ViewAPIProvider apiProvider) {
      // this.apiProvider = apiProvider;
      // // this.trees = new Vector<JTree>();
      // this.keyboardFactory =
      //     apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
    }
  
    private JComboBox<String> masterComboBox = new JComboBox<String>();
    private JPanel msg_panel = new JPanel();
  
    @Override
    public void buildUI(JPanel panel, ContributionProvider<ExternalControlProgramNodeContribution> provider) {
      System.out.println("---buildUI SuperNode---");
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(createDescription(description));
      infoLabel = new JLabel();
      panel.add(createMasterComboBox(masterComboBox, provider));
      // panel.add(createTopicComboBox(topicComboBox, provider));
      panel.add(createVertSeparator(50));
  
      panel.add(createMsgPanel());
      panel.add(createVertSeparator(20));
      panel.add(infoLabel);
      System.out.println("---end buildUI SuperNode---");
    }


    /////////////////////////////

    /*
    standardParamsPanel = new JPanel();
    JLabel standardParamsLabel = new JLabel("Standard Parameters: ");
    standardParamsLabel.setFont(new Font("Serif", Font.BOLD, 20));
    standardParamsPanel.add(standardParamsLabel);
    panel.add(standardParamsPanel);

    advancedParamsPanel = new JPanel();
    JLabel advancedParamsLabel = new JLabel("Advanced Parameters: ");
    advancedParamsLabel.setFont(new Font("Serif", Font.BOLD, 20));
    advancedParamsPanel.add(advancedParamsLabel);
    panel.add(advancedParamsPanel);

    createMaxLostPackages(
        standardParamsPanel, provider, maxLostPackages_TF, "Max Nr. of lost pkg: ", "1000");
    panel.add(createAdvancedParamBox(provider));

    createGainServoJ(advancedParamsPanel, provider, gainServoj_TF, "Gain servoj: ", "0");
     */
  // }

  public void setAdvancedParam_CB(boolean checked) {
    advancedParam_CB.setSelected(checked);
  }

  private void createMaxLostPackages(JPanel panel,
      final ContributionProvider<ExternalControlProgramNodeContribution> provider,
      final JTextField textField, String labelText, String defaultInput) {
    JLabel label = new JLabel(labelText);
    panel.add(label);

    textField.setText(defaultInput);
    textField.setFocusable(false);
    textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        KeyboardTextInput keyboardInput = provider.get().getInputForMaxLostPackages();
        keyboardInput.show(textField, provider.get().getCallbackForMaxLostPackages());
      }
    });
    panel.add(textField);
  }

  private void createGainServoJ(JPanel panel,
      final ContributionProvider<ExternalControlProgramNodeContribution> provider,
      final JTextField textField, String labelText, String defaultInput) {
    JLabel label = new JLabel(labelText);
    panel.add(label);

    textField.setText(defaultInput);
    textField.setFocusable(false);
    textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        KeyboardTextInput keyboardInput = provider.get().getInputForGainServoj();
        keyboardInput.show(textField, provider.get().getCallbackForGainServoj());
      }
    });
    panel.add(textField);
  }

  private Box createAdvancedParamBox(
      ContributionProvider<ExternalControlProgramNodeContribution> provider) {
    Box advancedParamBox = Box.createHorizontalBox();
    advancedParam_CB = new JCheckBox("Show advanced parameters");
    advancedParam_CB.setHorizontalTextPosition(SwingConstants.RIGHT);
    advancedParam_CB.setBorder(BorderFactory.createEmptyBorder());
    advancedParam_CB.addItemListener(getListenerForAdvancedParam(provider));
    advancedParam_CB.setMaximumSize(advancedParam_CB.getPreferredSize());
    advancedParamBox.add(advancedParam_CB);
    advancedParamBox.add(Box.createHorizontalGlue());
    return advancedParamBox;
  }

  public ItemListener getListenerForAdvancedParam(
      final ContributionProvider<ExternalControlProgramNodeContribution> provider) {
    return new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          provider.get().setAdvancedParam(true);
        } else {
          provider.get().setAdvancedParam(false);
        }
      }
    };
  }

  public void updateMaxLostPackages_TF(String value) {
    maxLostPackages_TF.setText(value);
  }

  public void updateGainServoj_TF(String value) {
    gainServoj_TF.setText(value);
  }

  public void showAdvancedParameters(boolean visible) {
    advancedParamsPanel.setVisible(visible);
  }

  public void updateInfoLabel(String host_ip, String custom_port) {
    infoLabel.setText("<html><body>"
        + "The program enabling external control is requested from the remote host.<br>"
        + "As to that, the parameters are currently set as follows: <br><br>"
        + "Host IP: " + host_ip + "<br>"
        + "Custom port: " + custom_port + "<br><br><br>"
        + "These settings can be altered via the Installation tab."
        + "<body><html>");
  }
//   private Box createMasterComboBox(
//     final JComboBox<String> combo, final ContributionProvider<C> provider) {
//   Box box = Box.createHorizontalBox();
//   box.setAlignmentX(Component.LEFT_ALIGNMENT);
//   JLabel label = new JLabel("Remote master");

//   combo.setPreferredSize(new Dimension(200, 30));
//   combo.setMaximumSize(combo.getPreferredSize());

//   combo.addItemListener(new ItemListener() {
//     @Override
//     public void itemStateChanged(ItemEvent e) {
//       if (e.getStateChange() == ItemEvent.SELECTED) {
//         provider.get().onMasterSelection((String) e.getItem());
//         updateMsgList(provider);
//       }
//     }
//   });

//   box.add(label);
//   box.add(createHorSpacer(10));
//   box.add(combo);

//   return box;
// }

  private Box createDescription(String desc) {
    Box box = Box.createHorizontalBox();
    box.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label = new JLabel(desc);
    box.add(label);

    return box;
  }

  private Component createVertSeparator(int height) {
    return Box.createRigidArea(new Dimension(0, height));
  }

  public JPanel createMsgPanel() {
    return createMsgPanel("Data");
  }
  public JPanel createMsgPanel(final String titel) {
    msg_panel.setLayout(new BoxLayout(msg_panel, BoxLayout.Y_AXIS));
    addLabel(titel, 20, msg_panel);
    return msg_panel;
  }

  public void addLabel(String text, int n, JPanel panel) {
    JLabel label = new JLabel(text);
    label.setFont(bold(n));
    panel.add(label);
  }

  protected Font bold(int n) {
    return new Font("Serif", Font.BOLD, n);
  }

  private Box createMasterComboBox(
    final JComboBox<String> combo, final ContributionProvider<ExternalControlProgramNodeContribution> provider) {
  Box box = Box.createHorizontalBox();
  box.setAlignmentX(Component.LEFT_ALIGNMENT);
  JLabel label = new JLabel("Remote master");

  combo.setPreferredSize(new Dimension(200, 30));
  combo.setMaximumSize(combo.getPreferredSize());

  combo.addItemListener(new ItemListener() {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        provider.get().onMasterSelection((String) e.getItem());
        // updateMsgList(provider);
      }
    }
  });

  box.add(label);
  box.add(createHorSpacer(10));
  box.add(combo);

  return box;
}

// public void updateMsgList(final ContributionProvider<ExternalControlProgramNodeContribution> provider) {
//   setTopicComboBoxItems(provider.get().queryMsgList());
// }

protected Component createHorSpacer(int width) {
  return Box.createRigidArea(new Dimension(width, 0));
}

// public void updateView(C contribution) {
//   setMasterComboBoxItems(contribution.getMastersList());
//   contribution.onMasterSelection((String) masterComboBox.getSelectedItem());
//   setTopicComboBoxItems(contribution.queryMsgList());
//   cleanPanel();

//   setTopicComboBoxSelection(contribution.getMsg());
//   setMasterComboBoxSelection(contribution.getMaster().toString());
// }

}