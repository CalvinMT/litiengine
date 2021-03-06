package de.gurkenlabs.litiengine.graphics.animation;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.ImageEffect;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;

public class AnimationController implements IAnimationController {
  private static final int MAX_IMAGE_EFFECTS = 20;
  private final Map<String, Animation> animations;
  private final List<ImageEffect> imageEffects;
  private final List<AnimationListener> listeners;

  private Animation currentAnimation;
  private Animation defaultAnimation;
  private AffineTransform affineTransform;
  private boolean enabled;

  public AnimationController() {
    this.animations = new ConcurrentHashMap<>();
    this.imageEffects = new CopyOnWriteArrayList<>();
    this.listeners = new CopyOnWriteArrayList<>();
    this.enabled = true;
  }

  public AnimationController(final Animation defaultAnimation) {
    this();
    this.setDefault(defaultAnimation);
  }

  public AnimationController(final Spritesheet sprite) {
    this(sprite, true);
  }

  public AnimationController(final Spritesheet sprite, boolean loop) {
    this(new Animation(sprite, loop, Resources.spritesheets().getCustomKeyFrameDurations(sprite)));
  }

  public AnimationController(final Animation defaultAnimation, final Animation... animations) {
    this(defaultAnimation);

    if (animations != null && animations.length > 0) {
      for (final Animation anim : animations) {
        if (anim != null) {
          this.animations.put(anim.getName(), anim);
        }
      }
    }
  }

  @Override
  public void add(final Animation animation) {
    if (animation == null) {
      return;
    }

    this.animations.put(animation.getName(), animation);
  }

  @Override
  public void add(final ImageEffect effect) {
    if (this.getImageEffects().size() >= MAX_IMAGE_EFFECTS) {
      return;
    }

    this.getImageEffects().add(effect);
    Collections.sort(this.getImageEffects());
  }

  public void attach() {
    Game.loop().attach(this);
    for (final Animation animation : this.getAll()) {
      Game.loop().attach(animation);
    }
  }

  public void detach() {
    Game.loop().detach(this);
    for (final Animation animation : this.getAll()) {
      Game.loop().detach(animation);
    }
  }

  @Override
  public void addListener(AnimationListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeListener(AnimationListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public AffineTransform getAffineTransform() {
    return this.affineTransform;
  }

  @Override
  public Collection<Animation> getAll() {
    return this.animations.values();
  }

  @Override
  public Animation get(String animationName) {
    if (animationName == null || animationName.isEmpty()) {
      return null;
    }

    return this.animations.getOrDefault(animationName, null);
  }

  @Override
  public Animation getCurrent() {
    return this.currentAnimation;
  }

  @Override
  public BufferedImage getCurrentSprite() {
    if (!this.isEnabled()) {
      return null;
    }

    final Animation current = this.getCurrent();
    if (current == null || current.getSpritesheet() == null || current.getCurrentKeyFrame() == null) {
      return null;
    }

    final String cacheKey = buildCurrentCacheKey();
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    BufferedImage sprite = current.getSpritesheet().getSprite(current.getCurrentKeyFrame().getSpriteIndex());
    for (final ImageEffect effect : this.getImageEffects()) {
      sprite = effect.apply(sprite);
    }

    return sprite;
  }

  @Override
  public BufferedImage getCurrentSprite(final int width, final int height) {
    if (this.getCurrentSprite() == null) {
      return null;
    }

    final String cacheKey = buildCurrentCacheKey() + "_" + width + "_" + height;
    Optional<BufferedImage> opt = Resources.images().tryGet(cacheKey);
    if (opt.isPresent()) {
      return opt.get();
    }

    return Imaging.scale(this.getCurrentSprite(), width, height);
  }

  @Override
  public Animation getDefault() {
    if (this.defaultAnimation != null) {
      return this.defaultAnimation;
    }

    if (this.getAll().isEmpty()) {
      return null;
    }

    return this.getAll().stream().findFirst().orElse(null);
  }

  @Override
  public List<ImageEffect> getImageEffects() {
    this.removeFinishedImageEffects();
    return this.imageEffects;
  }

  @Override
  public boolean hasAnimation(String animationName) {
    if (animationName == null || animationName.isEmpty()) {
      return false;
    }

    return this.animations.containsKey(animationName);
  }

  @Override
  public boolean isPlaying(String animationName) {
    return this.getCurrent() != null && this.getCurrent().getName() != null && this.getCurrent().getName().equalsIgnoreCase(animationName);
  }

  @Override
  public void play(final String animationName) {
    // if we have no animation with the name or it is already playing, do nothing
    if (this.isPlaying(animationName) || !this.hasAnimation(animationName)) {
      return;
    }

    final Animation anim = this.get(animationName);
    if (anim == null) {
      return;
    }

    // ensure that only one animation is playing at a time
    if (this.getCurrent() != null) {
      this.getCurrent().terminate();
    }

    this.currentAnimation = anim;
    this.currentAnimation.start();

    for (AnimationListener listener : this.listeners) {
      listener.played(this.getCurrent());
    }
  }

  @Override
  public void remove(Animation animation) {
    if (animation == null) {
      return;
    }

    this.animations.remove(animation.getName());
    if (this.currentAnimation != null && this.currentAnimation.equals(animation)) {
      this.currentAnimation = null;
    }

    if (this.getDefault() != null && this.getDefault().equals(animation)) {
      this.setDefault(this.getAll().stream().findFirst().orElse(null));
    }
  }

  @Override
  public void remove(ImageEffect effect) {
    if (effect == null) {
      return;
    }

    this.imageEffects.remove(effect);
  }

  @Override
  public void setDefault(Animation defaultAnimation) {
    if (this.defaultAnimation != null) {
      this.animations.remove(this.defaultAnimation.getName());
      if (this.currentAnimation != null && this.currentAnimation.equals(this.defaultAnimation)) {
        this.currentAnimation = null;
      }
    }

    this.defaultAnimation = defaultAnimation;
    if (this.defaultAnimation != null) {
      this.animations.put(this.defaultAnimation.getName(), this.defaultAnimation);
    }
  }

  @Override
  public void update() {
    if (this.getCurrent() != null && this.getCurrent().isPaused()) {
      return;
    }

    final boolean playbackFinished = this.getCurrent() != null && !this.getCurrent().isPlaying();
    if (playbackFinished) {
      for (AnimationListener listener : this.listeners) {
        listener.finished(this.getCurrent());
      }
    }

    if (this.getCurrent() == null || playbackFinished) {
      if (this.getDefault() != null) {
        this.play(this.getDefault().getName());
      } else {
        this.currentAnimation = null;
      }
    }
  }

  protected String buildCurrentCacheKey() {
    if (this.getCurrent() == null || this.getCurrent().getCurrentKeyFrame() == null || this.getCurrent().getSpritesheet() == null) {
      return null;
    }
    final StringBuilder cacheKey = new StringBuilder();
    cacheKey.append(this.getCurrent().getSpritesheet().hashCode());
    cacheKey.append('_');
    cacheKey.append(this.getCurrent().getCurrentKeyFrame().getSpriteIndex());
    cacheKey.append('_');

    this.getImageEffects().forEach(x -> cacheKey.append(x.getName().hashCode()));
    return cacheKey.toString();
  }

  private void removeFinishedImageEffects() {
    final List<ImageEffect> effectsToRemove = new ArrayList<>();
    for (final ImageEffect effect : this.imageEffects) {
      if (effect == null) {
        continue;
      }

      if (effect.timeToLiveReached()) {
        effectsToRemove.add(effect);
      }
    }

    this.imageEffects.removeAll(effectsToRemove);
    this.imageEffects.removeAll(Collections.singleton(null));
  }

  @Override
  public void setAffineTransform(AffineTransform affineTransform) {
    this.affineTransform = affineTransform;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
