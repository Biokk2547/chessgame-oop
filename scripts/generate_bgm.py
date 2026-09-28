import math
import struct
import wave
import os

def midi_to_freq(m):
    return 440.0 * (2.0 ** ((m - 69) / 12.0))

def generate_chess_bgm(output_path, loop_duration=53.333, sample_rate=44100):
    total_samples = int(loop_duration * sample_rate)
    left_buf = [0.0] * total_samples
    right_buf = [0.0] * total_samples

    bpm = 72.0
    beat_sec = 60.0 / bpm

    # 16-measure chord progression (4 beats each = 64 beats)
    # Roots: C3 (48), Am (45), F (41), G (43), Em (40), Am (45), Dm (50), G (43)
    # Each chord lasts 2 measures (8 beats) or 1 measure (4 beats)
    progression = [
        # Measure 1-2: Cmaj9
        {"bass": 36, "pad": [48, 55, 59, 64, 67], "arp": [48, 55, 60, 64, 67, 71, 67, 64], "start_beat": 0, "len_beats": 8},
        # Measure 3-4: Am9
        {"bass": 33, "pad": [45, 52, 55, 60, 64], "arp": [45, 52, 57, 60, 64, 67, 64, 60], "start_beat": 8, "len_beats": 8},
        # Measure 5-6: Fmaj7#11
        {"bass": 41, "pad": [41, 48, 52, 57, 60], "arp": [41, 48, 53, 57, 60, 65, 60, 57], "start_beat": 16, "len_beats": 8},
        # Measure 7-8: Gsus4 -> G
        {"bass": 43, "pad": [43, 50, 55, 60, 62], "arp": [43, 50, 55, 59, 62, 67, 62, 59], "start_beat": 24, "len_beats": 8},
        # Measure 9-10: Em7
        {"bass": 40, "pad": [40, 47, 52, 55, 59], "arp": [40, 47, 52, 55, 59, 64, 59, 55], "start_beat": 32, "len_beats": 8},
        # Measure 11-12: Am7
        {"bass": 45, "pad": [45, 52, 57, 60, 64], "arp": [45, 52, 57, 60, 64, 69, 64, 60], "start_beat": 40, "len_beats": 8},
        # Measure 13-14: Dm9
        {"bass": 38, "pad": [41, 48, 53, 57, 62], "arp": [38, 45, 50, 53, 57, 62, 57, 53], "start_beat": 48, "len_beats": 8},
        # Measure 15-16: G13sus4 -> Cmaj7 resolution
        {"bass": 43, "pad": [43, 50, 53, 57, 60], "arp": [43, 50, 55, 59, 62, 65, 62, 59], "start_beat": 56, "len_beats": 8},
    ]

    # Melody notes (beat, midi_note, duration_beats, velocity)
    # Relaxed, peaceful acoustic piano & music box melody
    melody = [
        # Phrase 1: Cmaj9 -> Am9
        (0.0, 72, 2.5, 0.75), (2.5, 74, 1.5, 0.65), (4.0, 76, 3.0, 0.8), (7.0, 74, 1.0, 0.6),
        (8.0, 71, 2.5, 0.75), (10.5, 69, 1.5, 0.65), (12.0, 67, 3.0, 0.75), (15.0, 69, 1.0, 0.6),
        # Phrase 2: Fmaj7 -> Gsus4
        (16.0, 69, 2.0, 0.75), (18.0, 72, 2.0, 0.8), (20.0, 76, 2.5, 0.85), (22.5, 74, 1.5, 0.7),
        (24.0, 74, 3.0, 0.8), (27.0, 72, 1.0, 0.65), (28.0, 71, 3.0, 0.75), (31.0, 69, 1.0, 0.6),
        # Phrase 3: Em7 -> Am7
        (32.0, 67, 2.5, 0.75), (34.5, 71, 1.5, 0.7), (36.0, 74, 3.0, 0.8), (39.0, 72, 1.0, 0.65),
        (40.0, 69, 2.5, 0.75), (42.5, 72, 1.5, 0.7), (44.0, 76, 3.0, 0.85), (47.0, 77, 1.0, 0.7),
        # Phrase 4: Dm9 -> G7 -> resolution
        (48.0, 76, 2.0, 0.8), (50.0, 74, 2.0, 0.75), (52.0, 72, 2.0, 0.75), (54.0, 69, 2.0, 0.7),
        (56.0, 71, 2.5, 0.8), (58.5, 74, 1.5, 0.75), (60.0, 72, 3.5, 0.9),
    ]

    # Bell / Chime high sparkle accents
    sparkles = [
        (4.0, 84, 1.5, 0.45), (6.0, 88, 1.5, 0.4),
        (12.0, 83, 1.5, 0.45), (14.0, 86, 1.5, 0.4),
        (20.0, 88, 1.5, 0.5), (22.0, 86, 1.5, 0.45),
        (28.0, 83, 1.5, 0.45), (30.0, 81, 1.5, 0.4),
        (36.0, 86, 1.5, 0.45), (38.0, 84, 1.5, 0.4),
        (44.0, 88, 1.5, 0.5), (46.0, 89, 1.5, 0.45),
        (52.0, 84, 1.5, 0.45), (54.0, 81, 1.5, 0.4),
        (60.0, 84, 2.5, 0.55), (62.0, 88, 2.0, 0.5),
    ]

    def add_tone(buf, start_sample, freq, duration_sec, amp, attack_sec, decay_sec, pan, is_piano=False, is_bell=False, is_pad=False):
        num_samples = int(duration_sec * sample_rate)
        for i in range(num_samples):
            idx = (start_sample + i) % total_samples
            t = i / float(sample_rate)

            # Amplitude envelope
            if t < attack_sec:
                env = t / attack_sec
            else:
                rel_t = t - attack_sec
                decay_rate = decay_sec
                env = math.exp(-rel_t / decay_rate)

            # Timbre generation
            if is_piano:
                # Acoustic Piano model: rich harmonics with natural warm roll-off
                h1 = math.sin(2.0 * math.pi * freq * t)
                h2 = 0.50 * math.sin(2.0 * math.pi * freq * 2.0 * t) * math.exp(-t * 2.2)
                h3 = 0.25 * math.sin(2.0 * math.pi * freq * 3.0 * t) * math.exp(-t * 3.5)
                h4 = 0.12 * math.sin(2.0 * math.pi * freq * 4.0 * t) * math.exp(-t * 5.0)
                # Gentle warm chorus
                chorus = 0.25 * math.sin(2.0 * math.pi * (freq * 1.002) * t)
                sig = (h1 + h2 + h3 + h4 + chorus) / 2.0
            elif is_bell:
                # Music Box / Celesta: bright inharmonic partials
                h1 = math.sin(2.0 * math.pi * freq * t)
                h2 = 0.35 * math.sin(2.0 * math.pi * freq * 2.76 * t) * math.exp(-t * 2.5)
                h3 = 0.18 * math.sin(2.0 * math.pi * freq * 5.4 * t) * math.exp(-t * 4.0)
                sig = (h1 + h2 + h3) / 1.5
            elif is_pad:
                # Soft ambient pad: sine + soft 3rd harmonic
                h1 = math.sin(2.0 * math.pi * freq * t)
                h2 = 0.3 * math.sin(2.0 * math.pi * freq * 2.0 * t)
                h3 = 0.15 * math.sin(2.0 * math.pi * freq * 3.0 * t)
                sig = (h1 + h2 + h3) / 1.45
            else: # Bass
                h1 = math.sin(2.0 * math.pi * freq * t)
                h2 = 0.35 * math.sin(2.0 * math.pi * freq * 2.0 * t) * math.exp(-t * 1.2)
                sig = (h1 + h2) / 1.35

            val = sig * env * amp
            buf[0][idx] += val * (1.0 - pan)
            buf[1][idx] += val * pan

    bufs = (left_buf, right_buf)

    # 1. Synthesize Harmony & Background Arpeggios
    for chord in progression:
        start_beat = chord["start_beat"]
        len_beats = chord["len_beats"]

        # Bass notes on beat 0 and beat 4 of each chord
        for b_off in [0.0, 4.0]:
            b_sample = int((start_beat + b_off) * beat_sec * sample_rate)
            f_bass = midi_to_freq(chord["bass"])
            add_tone(bufs, b_sample, f_bass, beat_sec * 3.5, 0.38, 0.04, 2.0, 0.5)

        # Warm ambient pad
        pad_sample = int(start_beat * beat_sec * sample_rate)
        pad_dur = len_beats * beat_sec
        for m_note in chord["pad"]:
            f_pad = midi_to_freq(m_note)
            add_tone(bufs, pad_sample, f_pad, pad_dur, 0.09, 0.8, 4.0, 0.45, is_pad=True)

        # Gentle piano arpeggios: one note every 0.5 beat (8th notes)
        arp_notes = chord["arp"]
        for step in range(int(len_beats * 2)):
            arp_beat = start_beat + (step * 0.5)
            a_sample = int(arp_beat * beat_sec * sample_rate)
            m_note = arp_notes[step % len(arp_notes)]
            f_arp = midi_to_freq(m_note)
            pan = 0.35 + 0.3 * (step % 2) # subtle stereo alternating
            add_tone(bufs, a_sample, f_arp, beat_sec * 2.0, 0.18, 0.02, 1.4, pan, is_piano=True)

    # 2. Synthesize Piano Melody
    for (m_beat, m_note, dur_beats, vel) in melody:
        m_sample = int(m_beat * beat_sec * sample_rate)
        f_mel = midi_to_freq(m_note)
        dur_sec = dur_beats * beat_sec
        add_tone(bufs, m_sample, f_mel, dur_sec + 1.2, vel * 0.35, 0.025, 2.2, 0.52, is_piano=True)

    # 3. Synthesize Music Box Sparkles
    for (s_beat, s_note, dur_beats, vel) in sparkles:
        s_sample = int(s_beat * beat_sec * sample_rate)
        f_spk = midi_to_freq(s_note)
        dur_sec = dur_beats * beat_sec
        add_tone(bufs, s_sample, f_spk, dur_sec + 1.0, vel * 0.22, 0.015, 1.8, 0.65, is_bell=True)

    # 4. Apply Gentle Stereo Reverb & Space (Comb / Delay filters)
    delay_samples = int(0.26 * sample_rate) # 260ms delay
    decay = 0.32
    for i in range(total_samples):
        prev_idx = (i - delay_samples + total_samples) % total_samples
        left_buf[i] += right_buf[prev_idx] * decay * 0.5
        right_buf[i] += left_buf[prev_idx] * decay * 0.5

    # 5. Normalization with -1.5 dB peak headroom
    max_amp = 1e-6
    for i in range(total_samples):
        if abs(left_buf[i]) > max_amp: max_amp = abs(left_buf[i])
        if abs(right_buf[i]) > max_amp: max_amp = abs(right_buf[i])

    target_peak = 0.85
    scale = target_peak / max_amp

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with wave.open(output_path, 'wb') as wav:
        wav.setnchannels(2) # Stereo
        wav.setsampwidth(2) # 16-bit
        wav.setframerate(sample_rate)
        frames = bytearray()
        for i in range(total_samples):
            l_val = int(max(-1.0, min(1.0, left_buf[i] * scale)) * 32767)
            r_val = int(max(-1.0, min(1.0, right_buf[i] * scale)) * 32767)
            frames.extend(struct.pack('<hh', l_val, r_val))
        wav.writeframes(frames)
    print(f"Generated BGM: {output_path} ({total_samples} samples, {total_samples/sample_rate:.2f}s)")

if __name__ == '__main__':
    generate_chess_bgm("assets/music/bgm.wav")
    generate_chess_bgm("desktop/assets/music/bgm.wav")
