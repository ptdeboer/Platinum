/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.ui.actionmenu;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;

@Slf4j
public class ActionCmd {
    /**
     * Argument separator character used in argument lists.
     */
    public static final String ARGUMENT_SEPERATOR_CHAR = ",";

    public static final String COMMAND_SEPERATOR_CHAR = ":";


    public static ActionCmd create(ActionCmdType actionCmdType, Object... args) {
       List<String> setrArgs = Arrays.stream(args).map(Object::toString).collect(Collectors.toList());
       return new ActionCmd(null, actionCmdType,setrArgs.toArray(new String[0]));
    }

    // === //
    private final ActionCmdType actionCmdType;
    private final Object source;
    private final String args[];

    public ActionCmd(Object eventSource, ActionCmdType actionCmdType, String... args) {
        this.source = eventSource;
        this.actionCmdType = actionCmdType;
        this.args=args;
    }

    public ActionCmdType getActionCmdType() {
        return this.actionCmdType;
    }

    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(actionCmdType.toString());
        if ((args!=null) && (args.length>0)) {
            sb.append(COMMAND_SEPERATOR_CHAR);
            for (int i=0;i<args.length;i++) {
                sb.append(args[i]);
                if (i+1<args.length) {
                    sb.append(ARGUMENT_SEPERATOR_CHAR);
                }
             }
        }
        String str = sb.toString();
        log.debug("ActionCmd.toString:'{}'",str);
        return str;
    }

    public static ActionCmd createFrom(ActionEvent event) {
        String cmdStr = event.getActionCommand();
        String cmds[] = cmdStr.split(COMMAND_SEPERATOR_CHAR);

        String methodStr = null;
        String argsStr = null;

        if (cmds.length > 0)
            methodStr = cmds[0];

        if (cmds.length > 1)
            argsStr = cmds[1];

        ActionCmd action = new ActionCmd(event.getSource(), ActionCmdType.createFrom(methodStr),parseArgs(argsStr));
        action.parseArgs(argsStr);

        log.debug("ActionCmd.createFrom():'{}' => '{}'",cmdStr,action.toString());
        return action;
    }

    protected static String[] parseArgs(String argsStr) {
        if ((argsStr == null) || argsStr.equals(""))
            return null;

        return StringList.createFrom(argsStr, ARGUMENT_SEPERATOR_CHAR).toArray();
    }

    public String[] getArgs() {
        return this.args;
    }

    public String getArg0() {
        if ((args == null) || (args.length < 1))
            return null;

        return this.args[0];
    }

    public String getArg1() {
        if ((args == null) || (args.length < 2)) {
            return null;
        }
        return args[1];
    }

    public Object getEventSource() {
        return source;
    }

    // === 
    // Factory method 
    // ===

    public static ActionCmd createSelectionAction(ViewNode node) {
        String arg = null;
        if (node != null) {
            arg = node.getVRL().toString();
        }
        ActionCmd action = new ActionCmd(null, ActionCmdType.SELECTION_ACTION, arg);
        return action;
    }

    public static ActionCmd createDefaultAction(ViewNode node) {
        String arg = null;
        if (node != null) {
            arg = node.getVRL().toString();
        }
        ActionCmd action = new ActionCmd(null, ActionCmdType.DEFAULT_ACTION, arg);
        return action;
    }

    public static ActionCmd createGlobalAction(ActionCmdType meth) {
        ActionCmd action = new ActionCmd(null, meth);
        return action;
    }

}
