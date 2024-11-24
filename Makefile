JFLAGS = -g
JC = javac
default: Main.java CASSwitch.java CCSShadowSwitch.java CCSSwitch.java Firewall.java Node.java Frame.java

Main.class: Main.java
	$(JC) $(JFLAGS) Main.java

CASSwitch.class: CASSwitch.java
	$(JC) $(JFLAGS) CASSwitch.java

CCSShadowSwitch.class: CCSShadowSwitch.java
	$(JC) $(JFLAGS) CASShadowSwitch.java

CCSSwitch.class: CCSSwitch.java
	$(JC) $(JFLAGS) CCSSwitch.java

Firewall.class: Firewall.java
	$(JC) $(JFLAGS) Firewall.java

Node.class: Node.java
	$(JC) $(JFLAGS) Node.java

Frame.class: Frame.java
	$(JC) $(JFLAGS) Frame.java

clean:
	$(RM) *.class
