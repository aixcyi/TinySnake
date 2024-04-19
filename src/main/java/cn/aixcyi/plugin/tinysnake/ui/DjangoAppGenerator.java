package cn.aixcyi.plugin.tinysnake.ui;

import cn.aixcyi.plugin.tinysnake.storage.DjangoAppGeneration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.PyNames;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import static cn.aixcyi.plugin.tinysnake.Zoo.message;

/**
 * Django App 初始化设置。
 *
 * @author <a href="https://github.com/aixcyi">砹小翼</a>
 */
public class DjangoAppGenerator extends DialogWrapper {

    public DjangoAppGeneration.State creation;

    private JPanel            contentPanel;
    private JTextField        nameField;
    private JTextField        labelField;
    private JComboBox<String> defaultAutoField;
    private ButtonGroup       adminGroup;
    private ButtonGroup       appsGroup;
    private ButtonGroup       modelsGroup;
    private ButtonGroup       serializersGroup;
    private ButtonGroup       testsGroup;
    private ButtonGroup       viewsGroup;
    private ButtonGroup       urlsGroup;
    private boolean           customizing = false;  // 用于控制是否不同步 App name 和 label
    private String            autoGenText = "";  // 用于判断是否应该断开同步

    public DjangoAppGenerator(Project project) {
        super(true);
        setResizable(true);
        setTitle(message("command.GenerateDjangoApp"));
        init();
        load(project);
    }

    public QualifiedName getName() {
        return QualifiedName.fromComponents(nameField.getText().split("\\."));
    }

    public void setName(QualifiedName baseQName) {
        final String text = baseQName.toString();
        if (!text.isEmpty()) {
            nameField.setText(text + ".");
            nameField.setCaretPosition(nameField.getText().length());
        }
    }

    public String getLabel() {
        return labelField.getText();
    }

    private void load(Project project) {
        creation = DjangoAppGeneration.getInstance(project).getState();
        defaultAutoField.getEditor().setItem(creation.getDefaultAutoField());
        updateButtonGroupSelection(adminGroup, creation.getAdmin());
        updateButtonGroupSelection(appsGroup, creation.getApps());
        updateButtonGroupSelection(modelsGroup, creation.getModels());
        updateButtonGroupSelection(serializersGroup, creation.getSerializers());
        updateButtonGroupSelection(testsGroup, creation.getTests());
        updateButtonGroupSelection(viewsGroup, creation.getViews());
        updateButtonGroupSelection(urlsGroup, creation.getUrls());
    }

    private void save() {
        creation.setDefaultAutoField((String) defaultAutoField.getEditor().getItem());
        creation.setAdmin(detectButtonGroupSelection(adminGroup));
        creation.setApps(detectButtonGroupSelection(appsGroup));
        creation.setModels(detectButtonGroupSelection(modelsGroup));
        creation.setSerializers(detectButtonGroupSelection(serializersGroup));
        creation.setTests(detectButtonGroupSelection(testsGroup));
        creation.setViews(detectButtonGroupSelection(viewsGroup));
        creation.setUrls(detectButtonGroupSelection(urlsGroup));
    }

    /**
     * 更新按钮组中单选框的选中状态。
     *
     * @param group    包含 {@link JRadioButton} 的按钮组。
     * @param creation 创建方式。
     */
    private void updateButtonGroupSelection(ButtonGroup group, DjangoAppGeneration.Creation creation) {
        Enumeration<AbstractButton> elements = group.getElements();
        while (elements.hasMoreElements()) {
            final AbstractButton element = elements.nextElement();
            element.setSelected(creation == DjangoAppGeneration.Creation.valueOf(element.getActionCommand()));
        }
    }

    /**
     * 从按钮组中检测创建方式。
     *
     * @param group 包含 {@link JRadioButton} 的按钮组。
     * @return 创建方式。
     */
    private DjangoAppGeneration.Creation detectButtonGroupSelection(ButtonGroup group) {
        Enumeration<AbstractButton> elements = group.getElements();
        while (elements.hasMoreElements()) {
            final AbstractButton element = elements.nextElement();
            if (element.isSelected())
                return DjangoAppGeneration.Creation.valueOf(element.getActionCommand());
        }
        return DjangoAppGeneration.Creation.EMPTY;
    }

    @Override
    protected void init() {
        super.init();
        nameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (customizing)
                    return;
                String[] parts = nameField.getText().split("\\.");
                if (parts.length > 0) {
                    autoGenText = parts[parts.length - 1];
                    labelField.setText(autoGenText);
                }
            }
        });
        labelField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!autoGenText.equals(labelField.getText()))
                    customizing = true;
            }
        });
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return nameField;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        final String name = nameField.getText();
        final String label = labelField.getText();
        if (name.isEmpty() || !name.matches("^\\w+(\\.\\w+)*$"))
            return new ValidationInfo(message("validation.info.IllegalPackageName"), nameField);
        if (label.isEmpty() || !PyNames.isIdentifier(label))
            return new ValidationInfo(message("validation.info.AppMustBeAnIdentifier"), labelField);
        return null;
    }

    @Override
    public boolean showAndGet() {
        if (super.showAndGet()) {
            this.save();
            return true;
        }
        return false;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        final JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}