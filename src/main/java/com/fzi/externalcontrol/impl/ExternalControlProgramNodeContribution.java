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

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.ProgramAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.undoredo.UndoRedoManager;
import com.ur.urcap.api.domain.undoredo.UndoableChanges;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

public class ExternalControlProgramNodeContribution implements ProgramNodeContribution {
  private static final String ADVANCED_PARAM_KEY = "showadvancedparam";
  private static final boolean ADVANCED_PARAM_DEFAULT = false;
  private static final String MAX_LOST_PACKAGES = "maxlostpackages";
  private static final String MAX_LOST_PACKAGES_DEFAULT_VALUE = "1000";
  private static final String GAIN_SERVO_J = "gain_servo_j";
  private static final String GAIN_SERVO_J_DEFAULT_VALUE = "0";
  private final ProgramAPI programAPI;
  private final DataModel model;
  private final ExternalControlProgramNodeView view;
  private final KeyboardInputFactory keyboardFactory;
  private final UndoRedoManager undoRedoManager;

  private static final String MASTER_KEY = "MASTER";
  private static final String MASTER_NAME_KEY = "MASTER_NAME";
  private static final String PORT_KEY = "PORT";


  private static final String DEFAULT_MASTER = "";
  private static final String DEFAULT_MASTER_NAME = "";
  private static final String DEFAULT_PORT = "";

  public ExternalControlProgramNodeContribution(
      ProgramAPIProvider apiProvider, ExternalControlProgramNodeView view, DataModel model) {
    this.programAPI = apiProvider.getProgramAPI();
    this.undoRedoManager = apiProvider.getProgramAPI().getUndoRedoManager();
    this.keyboardFactory =
        apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
    this.model = model;
    this.view = view;
  }

  @Override
  public void openView() {
    view.updateInfoLabel(getMasterIP(), getMasterPort());
  }

  @Override
  public void closeView() {}

  @Override
  public String getTitle() {
    return "Control by " + getInstallation().getName();
  }

  @Override
  public boolean isDefined() {
    return true;
  }

  @Override
  public void generateScript (ScriptWriter writer) {
    String controlLoop = getInstallation().getControlLoop(writer);
    writer.appendRaw(controlLoop);
  }

  private RosbridgeInstallationNodeContribution getInstallation() {
    return programAPI.getInstallationNode(RosbridgeInstallationNodeContribution.class);
  }

  public void setParam(final String key, final String value, final String default_val) {
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        if ("".equals(value)) {
          resetToDefaultValue(key, default_val);
        } else {
          model.set(key, value);
        }
      }
    });
  }

  public String getParam(String key, String default_val) {
    return model.get(key, default_val);
  }

  private void resetToDefaultValue(String key, String default_val) {
    model.set(key, default_val);
  }

  private boolean getAdvancedParam() {
    return model.get(ADVANCED_PARAM_KEY, ADVANCED_PARAM_DEFAULT);
  }

  public void setAdvancedParam(final boolean show) {
    updateAdvancedParam(show);
    // UndoRedoManager is necessary in program node but not in installation node
    // (see here:
    // https://plus.universal-robots.com/apidoc/40237/com/ur/urcap/api/domain/undoredo/undoredomanager.html)
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(ADVANCED_PARAM_KEY, show);
      }
    });
  }

  private void updateAdvancedParam(boolean enable) {
    if (enable) {
      view.showAdvancedParameters(true);
    } else {
      view.showAdvancedParameters(false);
    }
  }

  public KeyboardTextInput getInputForMaxLostPackages() {
    KeyboardTextInput keyboardInput = keyboardFactory.createStringKeyboardInput();
    keyboardInput.setInitialValue(getParam(MAX_LOST_PACKAGES, MAX_LOST_PACKAGES_DEFAULT_VALUE));
    return keyboardInput;
  }

  public KeyboardInputCallback<String> getCallbackForMaxLostPackages() {
    return new KeyboardInputCallback<String>() {
      @Override
      public void onOk(String value) {
        setParam(MAX_LOST_PACKAGES, value, MAX_LOST_PACKAGES_DEFAULT_VALUE);
        view.updateMaxLostPackages_TF(value);
      }
    };
  }

  public KeyboardTextInput getInputForGainServoj() {
    KeyboardTextInput keyboardInput = keyboardFactory.createStringKeyboardInput();
    keyboardInput.setInitialValue(getParam(GAIN_SERVO_J, GAIN_SERVO_J_DEFAULT_VALUE));
    return keyboardInput;
  }

  public KeyboardInputCallback<String> getCallbackForGainServoj() {
    return new KeyboardInputCallback<String>() {
      @Override
      public void onOk(String value) {
        setParam(GAIN_SERVO_J, value, GAIN_SERVO_J_DEFAULT_VALUE);
        view.updateGainServoj_TF(value);
      }
    };
  }

  public void onMasterSelection(final String selected_master) {
    System.out.println("### onMasterSelection");
    final MasterPair master = MasterPair.fromString(selected_master);
    if (getMasterIP().equals(master.getIp())
        && getMasterPort().equals(master.getPort())) { // no changes
      return;
    }
    System.out.println("Set model master to " + master.toString());
    undoRedoManager.recordChanges(new UndoableChanges() {
      @Override
      public void executeChanges() {
        model.set(MASTER_NAME_KEY, master.getName());
        model.set(MASTER_KEY, master.getIp());
        model.set(PORT_KEY, master.getPort());
      }
    });
  }

    protected String getMasterIP() {
    return model.get(MASTER_KEY, DEFAULT_MASTER);
  }

  protected String getMasterPort() {
    return model.get(PORT_KEY, DEFAULT_PORT);
  }

  // protected String[] queryMsgList() {
  //   System.out.println("### getMsgList");
  //   String[] items = new String[1];
  //   items[0] = getMsg();

  //   String request_string = getMsgListRequestString();

  //   try {
  //     // JSON parsing
  //     JSONObject json_response = rosbridgeRequest(request_string);
  //     if (json_response == null) {
  //       throw new NullPointerException("Response null");
  //     }
  //     JSONArray msgs =
  //         json_response.getJSONObject("values").getJSONArray(getMsgListResponsePlaceholder());
  //     items = new String[msgs.length() + 1];
  //     items[0] = " ";
  //     for (int i = 0, l = msgs.length(); i < l; ++i) {
  //       items[i + 1] = msgs.getString(i);
  //     }
  //   } catch (org.json.JSONException e) {
  //     System.err.println("getMsgList: JSON-Error: " + e);
  //   } catch (java.lang.Exception ex) {
  //     System.err.println("getMsgList: Error: " + ex);
  //   }

  //   Arrays.sort(items);
  //   return items;
  // }

  // protected String getMsg() {
  //   return model.get(MSG_KEY, DEFAULT_MSG);
  // }

}
