package jp.vmi.selenium.selenese.subcommand;

import jp.vmi.selenium.selenese.Context;
import jp.vmi.selenium.selenese.command.ArgumentType;

/**
 * Re-implementation of GetElementHeight.
 */
public class GetElementHeight extends AbstractSubCommand<Integer> {

    private static final int ARG_LOCATOR = 0;

    /**
     * Constructor.
     */
    public GetElementHeight() {
        super(ArgumentType.LOCATOR);
    }

    @Override
    public Integer execute(Context context, String... args) {
        return context.findElement(args[ARG_LOCATOR]).getSize().getHeight();
    }
}
