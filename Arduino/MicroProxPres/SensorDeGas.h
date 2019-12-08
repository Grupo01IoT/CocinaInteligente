#ifndef SENSORDEGAS_YA_INCLUIDO

#define SENSORDEGAS_YA_INCLUIDO

class SensorDeGas {
    
    private:
    
    int pin;
    
    float k;
    
    
    
    public:
    
    SensorDeGas(const int pin, const float k);
    
    void begin();
    
    bool hayGas();
};

#endif
