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

public class BardVisSlideshow extends PApplet {

	public static void main(String[] args) {
		PApplet.main("visual.BardVisSlideshow");
	}

	private PShape model;
	private float angle;
	private PGraphics modelLayer;
	private PGraphics waveLayer;
	private SoundFile music;
	private Waveform musicWave;
//	private Waveform speechWave;
	private int samples;
	private float h = 0;
	private PShape waveform;
	private Amplitude amp;
	private float waveMult = 10;

	private CastingSolutionRunner runner;
	private Thread thr;

	private int castIndex = 0;

	private PFont myFont;
	private int shadowDist = 8;

	private int mode = 0;

	private CastingSolution sln;

	public void settings() {
		fullScreen(P3D);
//		size(640, 480, P3D);
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
//		speech = new SoundFile(this, "shakes.wav");
		music.amp(0.1f);
		musicWave = new Waveform(this, samples);
		musicWave.input(music);
		music.loop();
		amp = new Amplitude(this);
		amp.input(music);
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
		if (!runner.isDone() || runner.isSolving()) {
			drawText("Calculating...", height * 200f / 1080f, 0, -height / 4);
		} else {
			drawText();
		}
	}

	public void keyPressed() {
		if (runner.isDone()) {
			if (keyCode == RIGHT)
				castIndex = min(castIndex + 1, sln.getActors().size());
			else if (keyCode == LEFT) {
				castIndex = max(castIndex - 1, 0);
			}

		} else {
			runner.terminateEarly();
			sln = runner.getBestSolution();
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
		waveLayer.shape(makeWaveform(musicWave));
		waveLayer.endDraw();
		image(waveLayer, 0, 0);
	}

	private PShape makeWaveform(Waveform wave) {
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

	private void drawText(String msg, float size, float x, float y) {
		drawText(msg, size, x, y, width, height);
	}

	private void drawText(String msg, float size, float x, float y, float w, float h) {
		textFont(myFont);
		textSize(size);
		textAlign(CENTER, CENTER);
		fill(0, 200);
		text(msg, x + shadowDist, y + shadowDist, w, h);
		fill(255);
		text(msg, x, y, w, h);
	}

	private void drawText() {
		if (castIndex < sln.getActors().size()) {
			Actor curr = sln.getActors().get(castIndex);
			List<Role> roles = sln.getCastMap().get(curr);
			drawText(curr.getName(), height * 200f / 1080f, 0, height / 8, width, height / 4);
			drawText("as", height * 100f / 1080f, 0, 0);
			drawText(combineRoles(roles), height * 100f / 1080f, width / 15, height / 2, 13 * width / 15, height / 2);
		} else {
			drawText("Enjoy the show!", height * 200f / 1080f, 0, 0);
		}
	}
}
