package org.jetbrains.training;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 18/12/14.
 */
public class ActionsRecorder implements Disposable {


    private Project project;
    private Document document;
    private String target;
    private boolean triggerActivated;

    private boolean disposed = false;

    public ActionsRecorder(Project project, Document document, String target) {
        this.project = project;
        this.document = document;
        this.target = target;
        this.triggerActivated = false;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    public void startRecording(final Runnable showWinMessage){

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {

            }

            @Override
            public void documentChanged(DocumentEvent event) {

//                Notification notification = new Notification("IDEA Global Help", "document changed", "document changed", NotificationType.INFORMATION);
//                Notifications.Bus.notify(notification);

                if (isTaskSolved(document, target)) {
//                    System.err.println("Task is solved!");
                    //myInfoPanel.setText("Cool, let's try yourself now!");
//                    Messages.showMessageDialog(project, "Congratulations, this task has been solved!", "Information", Messages.getInformationIcon());
                    showWinMessage.run();
                }
            }
        };

        document.addDocumentListener(documentListener, this);
    }

    public void startRecording(final Runnable showWinMessage, final @Nullable String actionId) {
        if(actionId != null && !actionId.equals("") && ActionManager.getInstance().getAction(actionId) != null) {
            checkAction(actionId);
            startRecording(showWinMessage);
        } else {
            triggerActivated = true;
            startRecording(showWinMessage);
        }

    }

    public boolean isTaskSolved(Document current, String target){
        if (disposed) return false;

        List<String> expected = computeTrimmedLines(target);
        List<String> actual = computeTrimmedLines(current.getText());

        return ((expected.equals(actual) && triggerActivated));
    }

    private List<String> computeTrimmedLines(String s) {
        ArrayList<String> ls = new ArrayList<String>();

        for (String it :StringUtil.splitByLines(s) ) {
            it.trim();

            if (it.equals("")){
                //ls.add("NULL");
            } else {
                ls.add(it);
            }
        }

        return ls;

    }

    private void checkAction(final String actionTriggerId){

        final ActionManager actionManager = ActionManager.getInstance();
        if(actionManager == null) return;

        final AnActionListener anActionListener = new AnActionListener() {
            @Override
            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
            }

            @Override
            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                final String actionId = ActionManager.getInstance().getId(action);
                if (actionId.toUpperCase().equals(actionTriggerId.toUpperCase())) {
//                    System.err.println("Action trigger has been activated.");
                    triggerActivated = true;
                    actionManager.removeAnActionListener(this);
                }
//                System.err.println("ACTION PERFORMED: " + actionId);
            }

            @Override
            public void beforeEditorTyping(char c, DataContext dataContext) {
            }
        };

        actionManager.addAnActionListener(anActionListener);
    }
}

