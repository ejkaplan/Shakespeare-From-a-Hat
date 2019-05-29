package visual;

import java.util.List;

import casting.Actor;
import casting.CastingSolution;
import casting.CastingSolutionRunner;
import casting.Role;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.sound.Amplitude;
import processing.sound.SoundFile;
import processing.sound.Waveform;

public class BardVis extends PApplet {

	public static void main(String[] args) {
		PApplet.main("visual.BardVis");
	}

	private PShape model;
	private float angle;
	private PGraphics modelLayer;
	private PGraphics waveLayer;
	private SoundFile music;
	private Waveform wave;
	private int samples;
	private float h = 0;
	private PShape waveform;
	private Amplitude amp;
	private float smoothingFactor = 0.5f;
	private float ampSum = 0;
	private float waveMult = 10;

	private CastingSolutionRunner runner;
	private Thread thr;

	private int castIndex = -1;
	private int changeTime = 0;
	private int changeInterval = 5000;

	private PFont myFont;
	private int shadowDist = 8;

	public void settings() {
//		size(640, 480, P3D);
		fullScreen(P3D);
	}

	public void setup() {
		colorMode(HSB);
		samples = round(width / 2.5f);
		modelLayer = createGraphics(width, height, P3D);
		waveLayer = createGraphics(width, height);
		model = loadShape("shakespeare_lowpoly.obj");
		model.setFill(color(0));
		model.setFill(true);
		model.setStroke(true);
		model.setStrokeWeight(0.04f);
		model.scale(height / 20);
		model.rotateX(PI);
		model.rotateY(PI);
		smooth();
		music = new SoundFile(this, "suite_jam.wav");
		music.amp(0.1f);
		music.loop();
		wave = new Waveform(this, samples);
		wave.input(music);
		amp = new Amplitude(this);
		amp.input(music);
		myFont = createFont("forced square", 200);
		textAlign(CENTER);
		runner = new CastingSolutionRunner("roles.csv", "actors.csv");
		thr = new Thread(runner);
		thr.start();
	}

	public void draw() {
		background(0);
		drawWaveform();
		drawModel();
		angle += 0.005f;
		h += 0.5;
		if (h > 255)
			h -= 255;
		if (runner.isDone()) {
			CastingSolution sln = runner.getBestSolution();
			if (millis() > changeTime) {
				castIndex = (castIndex + 1) % sln.getActors().size();
				changeTime = millis() + changeInterval;
			}
			Actor curr = sln.getActors().get(castIndex);
			List<Role> roles = sln.getCastMap().get(curr);
			textFont(myFont);
			fill(0, 200);
			text(curr.getName(), shadowDist, height / 8 + shadowDist, width, height / 4);
			fill(255);
			text(curr.getName(), 0, height / 8, width, height / 4);
			textSize(100);
			fill(0, 200);
			text("as", width / 2 + shadowDist, 3 * height / 8 + shadowDist);
			fill(255);
			text("as", width / 2, 3 * height / 8);
			textSize(150);
			fill(0, 200);
			text(combineRoles(roles), width / 20 + shadowDist, height / 2 + shadowDist, 18 * width / 20, height / 2);
			fill(255);
			text(combineRoles(roles), width / 20, height / 2, 18 * width / 20, height / 2);
		}
	}

	private void drawModel() {
		modelLayer.beginDraw();
		modelLayer.lights();
		modelLayer.clear();
		modelLayer.translate(width / 2, height / 2);
		modelLayer.rotateY(angle);
		float modelH = h + 255f / 2f;
		if (modelH > 255)
			modelH -= 255;
//		model.setStroke(color(modelH, 255, 255*getSmoothedAmp()));
		model.setStroke(color(modelH, 255, 255));
		modelLayer.shape(model, 0, 0);
		modelLayer.endDraw();
		image(modelLayer, 0, 0);
	}

	private void drawWaveform() {
		waveLayer.beginDraw();
		waveLayer.colorMode(HSB);
		waveLayer.clear();
		waveLayer.noFill();
		waveLayer.stroke(h, 255, 255);
		waveLayer.strokeWeight(5);
		waveLayer.shape(makeWaveform());
		waveLayer.endDraw();
		image(waveLayer, 0, 0);
	}

	private PShape makeWaveform() {
		wave.analyze();
		if (waveform == null) {
			waveform = createShape();
			waveform.beginShape();
			for (int i = 0; i < samples; i++) {
				waveform.vertex(map(i, 0, samples - 1, 0, width),
						map(wave.data[i], -1.0f / waveMult, 1.0f / waveMult, 0, height));
			}
			waveform.endShape();
			waveform.disableStyle();
		} else {
			for (int i = 0; i < samples; i++) {
				PVector v = waveform.getVertex(i);
				PVector w = new PVector(map(i, 0, samples - 1, 0, width),
						map(wave.data[i], -1.0f / waveMult, 1.0f / waveMult, 0, height));
				w.lerp(v, 0.9f);
				waveform.setVertex(i, w);
			}
		}
		return waveform;
	}

	private float getSmoothedAmp() {
		ampSum += (amp.analyze() - ampSum) * smoothingFactor;
		return ampSum;
	}

	private String combineRoles(List<Role> roles) {
		String out = "";
		for (int i = 0; i < roles.size(); i++) {
			out += roles.get(i).getName();
			if (roles.size() > i + 1)
				out += ", ";
			if (roles.size() == i + 2)
				out += "and ";
		}
		return out;
	}
}
