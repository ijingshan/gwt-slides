package org.gquery.slides.client;

import static com.google.gwt.query.client.GQuery.*;
import static org.gquery.slides.client.GQ.hash;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Predicate;
import com.google.gwt.user.client.Event;

/**
 * Entry point for the presentation
 */
public class Slides implements EntryPoint {

  private static final String PRESENT = "present";
  private static final String PAST = "past";
  private static final String FUTURE = "future";
  private static final String DISPLAY_PLAY_BUTTON = "displayPlayButton";
  private static final String CODE_SNIPPET = "<div class='CodeMirror'><div class='code-scroll " +
      "code-div'><div class='code-lines'><pre>%code%</pre></div></div></div>";

  private int currentPage = 1;
  private String currentExecId = null;
  private SlidesDeferred examplesClass;
  private GQuery slides;

  public void onModuleLoad() {
    examplesClass = GWT.create(SlidesDeferred.class);

    // build slide
    $(".slides > section").each(
        new Function() {
          public void f(Element e) {
            buildSlide($(this));
          }
        });

    bindEvents();

    // filter the empty slides
    slides = $(".slides > section").filter(new Predicate() {
      @Override
      public boolean f(Element e, int index) {
        return (!$(e).html().trim().isEmpty());
      }
    });

    showCurrentSlide();
  }

  private void showCurrentSlide() {
    String hash = hash();
    currentPage = hash.matches("\\d+") ? Integer.parseInt(hash) : 0;
    updatePageControl();

    slides.hide().each(new Function() {
      @Override
      public Object f(Element e, int i) {
        GQuery slide = $(e).removeClass(PAST, PRESENT, FUTURE);

        if (i < currentPage) {
          slide.addClass(PAST);
        } else if (i > currentPage) {
          slide.addClass(FUTURE);
        } else {
          slide.addClass(PRESENT);
        }
        return null;
      }
    });

    // After 1 sec, all css animations are done,  presentation is ready -> show the slides
    slides.delay(1000, lazy().show().done());
  }

  private void bindEvents() {
    $(window).bind(Event.ONKEYDOWN, new Function() {
      public boolean f(Event e) {
        if (e.getKeyCode() == KeyCodes.KEY_RIGHT) {
          return show(true);
        }
        if (e.getKeyCode() == KeyCodes.KEY_LEFT) {
          return show(false);
        }
        return true;
      }
    });

    $("#play").click(new Function() {
      public boolean f(Event e) {
        run();
        return false;
      }
    });
  }

  private void buildSlide(GQuery slide) {
    String html = slide.html();
    String id = slide.id().toLowerCase();

    String javadoc = examplesClass.docs.get(id);
    String code = examplesClass.snippets.get(id);

    if (javadoc != null){
      html += javadoc;
    }

    boolean displayPlayButton = true;
    if (code != null && code.trim().length() > 0) {
      html += CODE_SNIPPET.replace("%code%", Prettify.prettify(code));
    } else {
      displayPlayButton = false;
    }

    slide.data(DISPLAY_PLAY_BUTTON, displayPlayButton);
    slide.html(html);
  }

  private void run() {
    console.clear();
    examplesClass.exec(currentExecId);
  }

  private boolean show(boolean forward) {
    int incr = forward ? 1 : -1;
    int nextPage = Math.min(Math.max(currentPage + incr, 0), slides.size());
    String classForOldSlide = forward ? PAST : FUTURE;

    console.clear();

    slides.eq(currentPage).removeClass(PRESENT).addClass(classForOldSlide);

    GQuery nextSlide = slides.eq(nextPage);
    nextSlide.removeClass(FUTURE, PAST).addClass(PRESENT);

    currentPage = nextPage;
    updatePageControl();

    return false;
  }

  private void hideOrShowPlayButton(GQuery slide) {
    if (slide.data(DISPLAY_PLAY_BUTTON, Boolean.class)) {
      $("#play").show();
    } else {
      $("#play").hide();
    }
  }

  private void updatePageControl() {
    GQuery slide = slides.eq(currentPage);
    currentExecId = slide.id();
    hideOrShowPlayButton(slide);

    // Update page number
    String page = "" + currentPage;
    $("#marker").text(page);

    hash(page);
  }
}
