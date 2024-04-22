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
 * @see <a href="https://docs.djangoproject.com/zh-hans/5.0/ref/applications/">应用程序及其配置</a>
 * @see <a href="https://docs.djangoproject.com/en/5.0/ref/applications/">Application & configuration</a>
 */
public class DjangoAppGenerator extends DialogWrapper {

    // ---- 公开状态 ----
    public final DjangoAppGeneration.State creation;

    // ---- 窗口组件 ----
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

    // ---- 内部状态 ----
    private boolean customizing = false;  // 用于控制是否不同步 App name 和 label
    private String  autoGenText = "";  // 用于判断是否应该断开同步

    public DjangoAppGenerator(Project project) {
        super(true);
        setResizable(true);
        setTitle(message("command.GenerateDjangoApp"));
        creation = DjangoAppGeneration.getInstance(project).getState();
        init();
        load();
    }

    /**
     * 获取 Django App 包的路径。
     *
     * @return 包路径。Kotlin 中可以通过 {@code for c in this.name.components} 进行枚举。
     * @see <a href="https://docs.djangoproject.com/zh-hans/5.0/ref/applications/#django.apps.AppConfig.name">AppConfig.name</a>
     */
    public QualifiedName getName() {
        return QualifiedName.fromComponents(nameField.getText().split("\\."));
    }

    /**
     * 设置 Django App 包所处目录的路径。
     * <p>
     * <ul>
     *     <li>假设希望在在 ./zeraora/user 包内插入一个 Django App，那么只需提供 {@code "zeraora.user"} 这样的包路径即可。</li>
     *     <li>提供非空路径后，比如 {@code "zeraora.user"} 会显示为 {@code "zeraora.user."}，方便用户输入包名。</li>
     * </ul>
     *
     * @param baseQName 包路径。可以通过 {@code QualifiedName.fromComponents("django.db.models".split("\\.")) } 这样的方式构造。
     * @see <a href="https://docs.djangoproject.com/zh-hans/5.0/ref/applications/#django.apps.AppConfig.name">AppConfig.name</a>
     */
    public void setName(QualifiedName baseQName) {
        final String text = baseQName.toString();
        if (!text.isEmpty()) {
            nameField.setText(text + ".");
            nameField.setCaretPosition(nameField.getText().length());
        }
    }

    /**
     * 获取 Django App 的简称。默认是 {@code name} 的最后一段。
     *
     * @return 一个字符串。
     * @see <a href="https://docs.djangoproject.com/zh-hans/5.0/ref/applications/#django.apps.AppConfig.label">AppConfig.label</a>
     */
    public String getLabel() {
        return labelField.getText();
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

    private void load() {
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
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return nameField;
    }

    @Override
    @Nullable
    protected ValidationInfo doValidate() {
        final String name = nameField.getText();
        final String label = labelField.getText();
        if (name.isEmpty() || !name.matches("^\\w+(\\.\\w+)*$"))
            return new ValidationInfo(message("validation.IllegalPackageName"), nameField);
        if (label.isEmpty() || !PyNames.isIdentifier(label))
            return new ValidationInfo(message("validation.AppMustBeAnIdentifier"), labelField);
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
    @Nullable
    protected JComponent createCenterPanel() {
        final JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(contentPanel, BorderLayout.CENTER);
        return dialogPanel;
    }
}