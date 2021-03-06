/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.sharedpasses.opti;

import com.google.template.soy.data.SoyRecord;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import com.google.template.soy.shared.restricted.SoyPurePrintDirective;
import com.google.template.soy.sharedpasses.render.RenderException;
import com.google.template.soy.sharedpasses.render.RenderVisitor;
import com.google.template.soy.soytree.CallDelegateNode;
import com.google.template.soy.soytree.CssNode;
import com.google.template.soy.soytree.DebuggerNode;
import com.google.template.soy.soytree.LogNode;
import com.google.template.soy.soytree.MsgFallbackGroupNode;
import com.google.template.soy.soytree.PrintDirectiveNode;
import com.google.template.soy.soytree.PrintNode;
import com.google.template.soy.soytree.SoyNode;
import com.google.template.soy.soytree.TemplateRegistry;
import com.google.template.soy.soytree.jssrc.GoogMsgDefNode;
import com.google.template.soy.soytree.jssrc.GoogMsgRefNode;

import java.util.Deque;
import java.util.Map;

import javax.annotation.Nullable;


/**
 * Visitor for prerendering the template subtree rooted at a given SoyNode. This is possible when
 * all data values are known at compile time.
 *
 * Package-private helper for {@link SimplifyVisitor}.
 *
 * <p> The rendered output will be appended to the Appendable provided to the constructor.
 *
 * @author Kai Huang
 */
class PrerenderVisitor extends RenderVisitor {


  /**
   * @param soyJavaDirectivesMap Map of all SoyJavaPrintDirectives (name to
   *     directive).
   * @param preevalVisitorFactory Factory for creating an instance of PreevalVisitor.
   * @param outputBuf The Appendable to append the output to.
   * @param templateRegistry A registry of all templates.
   * @param data The current template data.
   * @param env The current environment, or null if this is the initial call.
   */
  PrerenderVisitor(
      Map<String, SoyJavaPrintDirective> soyJavaDirectivesMap,
      PreevalVisitorFactory preevalVisitorFactory, Appendable outputBuf,
      @Nullable TemplateRegistry templateRegistry, SoyRecord data,
      @Nullable Deque<Map<String, SoyValue>> env) {

    super(
        soyJavaDirectivesMap, preevalVisitorFactory, outputBuf,
        templateRegistry, data, null, env, null, null, null, null);
  }


  @Override protected PrerenderVisitor createHelperInstance(Appendable outputBuf, SoyRecord data) {

    return new PrerenderVisitor(
        soyJavaDirectivesMap, (PreevalVisitorFactory) evalVisitorFactory, outputBuf,
        templateRegistry, data, null);
  }


  @Override public Void exec(SoyNode soyNode) {

    // Note: This is a catch-all to turn RuntimeExceptions that aren't RenderExceptions into
    // RenderExceptions during prerendering.

    try {
      return super.exec(soyNode);

    } catch (RenderException e) {
      throw e;

    } catch (RuntimeException e) {
      throw new RenderException("Failed prerender due to exception: " + e.getMessage(), e);
    }
  }


  // -----------------------------------------------------------------------------------------------
  // Implementations for specific nodes.


  @Override protected void visitMsgFallbackGroupNode(MsgFallbackGroupNode node) {
    throw new RenderException("Cannot prerender MsgFallbackGroupNode.");
  }


  @Override protected void visitGoogMsgDefNode(GoogMsgDefNode node) {
    throw new RenderException("Cannot prerender GoogMsgDefNode.");
  }


  @Override protected void visitGoogMsgRefNode(GoogMsgRefNode node) {
    throw new RenderException("Cannot prerender GoogMsgRefNode.");
  }


  @Override protected void visitCssNode(CssNode node) {
    throw new RenderException("Cannot prerender CssNode.");
  }


  @Override protected void visitCallDelegateNode(CallDelegateNode node) {
    throw new RenderException("Cannot prerender CallDelegateNode.");
  }


  @Override protected void visitLogNode(LogNode node) {
    throw new RenderException("Cannot prerender LogNode.");
  }


  @Override protected void visitDebuggerNode(DebuggerNode node) {
    throw new RenderException("Cannot prerender DebuggerNode.");
  }


  @Override protected void visitPrintNode(PrintNode node) {
    for (PrintDirectiveNode directiveNode : node.getChildren()) {
      if (!isSoyPurePrintDirective(directiveNode)) {
        throw new RenderException("Cannot prerender a node with some impure print directive.");
      }
    }
    super.visitPrintNode(node);
  }


  @Override protected void visitPrintDirectiveNode(PrintDirectiveNode node) {
    if (!isSoyPurePrintDirective(node)) {
      throw new RenderException("Cannot prerender impure print directive.");
    }
    super.visitPrintDirectiveNode(node);
  }


  private boolean isSoyPurePrintDirective(PrintDirectiveNode node) {
    SoyJavaPrintDirective directive = soyJavaDirectivesMap.get(node.getName());
    return directive != null &&
        directive.getClass().isAnnotationPresent(SoyPurePrintDirective.class);
  }

}
