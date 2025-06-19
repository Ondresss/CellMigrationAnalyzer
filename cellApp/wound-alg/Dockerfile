# Use Miniconda base image
FROM registry.codeocean.com/codeocean/miniconda3:4.12.0-python3.9-ubuntu20.04

# Set environment variable for non-interactive installs
ARG DEBIAN_FRONTEND=noninteractive

# Install necessary Python dependencies
RUN pip3 install -U --no-cache-dir \
    matplotlib==3.7.2 \
    numpy==1.25.2 \
    opencv-python-headless==4.8.0.74 \
    pandas==1.1.5 \
    scikit-image==0.21.0

# Copy the whole 'capsule<id>' directory into the container
COPY . /app

# Set the working directory to where the script is located
WORKDIR /app/code

# Default command to run when the container starts
CMD ["python3", "run_single_file_demo.py"]
