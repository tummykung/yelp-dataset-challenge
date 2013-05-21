package weka.gui.beans;

/**
 * Extends BeanCustomizer. Exists primarily for those customizers
 * that can be displayed in the GenericObjectEditor (in preference
 * to individual property editors). Provides a method to tell
 * the customizer not to show any OK and CANCEL buttons if being 
 * displayed in the GOE (since the GOE provides those). Also specifies
 * the methods for handling closing under OK or CANCEL conditions.
 * 
 * Implementers of this interface should *not* use the GOE internally 
 * to implement the customizer because this will result in an cyclic 
 * loop of initializations that will end up in a stack overflow when
 * the customizer is displayed by the GOE at the top level.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 48815 $
 */
public interface GOECustomizer extends BeanCustomizer {
  
  /**
   * Tells the customizer not to display its own OK and
   * CANCEL buttons
   */
  void dontShowOKCancelButtons();
  
  /**
   * Gets called when the customizer is closing under an OK
   * condition 
   */
  void closingOK();
  
  /**
   * Gets called when the customizer is closing under a
   * CANCEL condition
   */
  void closingCancel();
}
